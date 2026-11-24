package br.edu.uva.tcc.monitoramento.service;

import br.edu.uva.tcc.monitoramento.config.LimitesProperties;
import br.edu.uva.tcc.monitoramento.domain.Alerta;
import br.edu.uva.tcc.monitoramento.domain.Leitura;
import br.edu.uva.tcc.monitoramento.domain.TipoAlerta;
import br.edu.uva.tcc.monitoramento.dto.AlertaResponse;
import br.edu.uva.tcc.monitoramento.dto.DispositivoStatus;
import br.edu.uva.tcc.monitoramento.dto.LeituraRequest;
import br.edu.uva.tcc.monitoramento.dto.LeituraResponse;
import br.edu.uva.tcc.monitoramento.dto.RegistroResponse;
import br.edu.uva.tcc.monitoramento.repository.AlertaRepository;
import br.edu.uva.tcc.monitoramento.repository.LeituraRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonitoramentoService {

    /** Estado em memória de cada dispositivo. Todo acesso é feito dentro de synchronized(estado). */
    private static final class EstadoDispositivo {
        final DetectorAnomalia detector;
        Instant ultimaLeitura;
        boolean falhaComunicacao;
        double ultimaTemperatura;
        double ultimaCorrente;

        EstadoDispositivo(DetectorAnomalia detector) {
            this.detector = detector;
        }
    }

    private final LimitesProperties limites;
    private final LeituraRepository leituras;
    private final AlertaRepository alertas;
    private final Map<String, EstadoDispositivo> dispositivos = new ConcurrentHashMap<>();

    public MonitoramentoService(LimitesProperties limites, LeituraRepository leituras, AlertaRepository alertas) {
        this.limites = limites;
        this.leituras = leituras;
        this.alertas = alertas;
    }

    /** Grava a leitura, roda a detecção e persiste os alertas gerados. */
    @Transactional
    public RegistroResponse registrar(LeituraRequest req) {
        Instant agora = Instant.now();
        EstadoDispositivo estado = dispositivos.computeIfAbsent(
                req.dispositivoId(), id -> new EstadoDispositivo(new DetectorAnomalia(limites)));

        List<EventoAlerta> eventos;
        synchronized (estado) {
            estado.ultimaLeitura = agora;
            estado.falhaComunicacao = false; // voltou a comunicar
            estado.ultimaTemperatura = req.temperaturaC();
            estado.ultimaCorrente = req.correnteA();
            eventos = estado.detector.avaliar(req.temperaturaC(), req.correnteA());
        }

        Leitura leitura = leituras.save(new Leitura(req.dispositivoId(), req.temperaturaC(), req.correnteA(), agora));
        List<AlertaResponse> gerados = new ArrayList<>();
        for (EventoAlerta e : eventos) {
            Alerta a = alertas.save(new Alerta(req.dispositivoId(), e.tipo(), e.valor(), e.mensagem(), agora));
            gerados.add(AlertaResponse.de(a));
        }
        return new RegistroResponse(LeituraResponse.de(leitura), gerados);
    }

    /** Roda periodicamente e gera FALHA_COMUNICACAO se um dispositivo ficar mudo além do timeout. */
    @Scheduled(fixedDelayString = "${monitoramento.verificacao-comunicacao-ms:5000}")
    public void verificarComunicacao() {
        verificarComunicacao(Instant.now());
    }

    /** Versão com o instante injetado (facilita testar). */
    public void verificarComunicacao(Instant agora) {
        Duration timeout = limites.timeoutComunicacao();
        dispositivos.forEach((id, estado) -> {
            synchronized (estado) {
                boolean estourou = Duration.between(estado.ultimaLeitura, agora).compareTo(timeout) > 0;
                if (estourou && !estado.falhaComunicacao) {
                    estado.falhaComunicacao = true;
                    alertas.save(new Alerta(id, TipoAlerta.FALHA_COMUNICACAO, null,
                            "Sem dados do dispositivo há mais de " + timeout.toSeconds() + " s", agora));
                }
            }
        });
    }

    public Optional<DispositivoStatus> status(String dispositivoId) {
        EstadoDispositivo estado = dispositivos.get(dispositivoId);
        if (estado == null) {
            return Optional.empty();
        }
        synchronized (estado) {
            return Optional.of(new DispositivoStatus(
                    dispositivoId,
                    estado.ultimaLeitura,
                    !estado.falhaComunicacao,
                    estado.detector.getNivelTemperatura().name(),
                    estado.detector.getNivelCorrente().name(),
                    estado.detector.isAnomaliaAtiva(),
                    estado.ultimaTemperatura,
                    estado.ultimaCorrente));
        }
    }

    @Transactional(readOnly = true)
    public List<LeituraResponse> listarLeituras(String dispositivoId, int limite) {
        var pagina = PageRequest.of(0, limite);
        var lista = (dispositivoId == null || dispositivoId.isBlank())
                ? leituras.findAllByOrderByInstanteDesc(pagina)
                : leituras.findByDispositivoIdOrderByInstanteDesc(dispositivoId, pagina);
        return lista.stream().map(LeituraResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<AlertaResponse> listarAlertas(String dispositivoId, int limite) {
        var pagina = PageRequest.of(0, limite);
        var lista = (dispositivoId == null || dispositivoId.isBlank())
                ? alertas.findAllByOrderByInstanteDesc(pagina)
                : alertas.findByDispositivoIdOrderByInstanteDesc(dispositivoId, pagina);
        return lista.stream().map(AlertaResponse::de).toList();
    }
}
