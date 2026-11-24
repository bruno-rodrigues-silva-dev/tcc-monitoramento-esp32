package br.edu.uva.tcc.monitoramento.service;

import br.edu.uva.tcc.monitoramento.config.LimitesProperties;
import br.edu.uva.tcc.monitoramento.domain.TipoAlerta;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

/**
 * Lógica de detecção de um dispositivo (uma instância por ESP32). Classe sem dependência do Spring,
 * para poder ser testada isoladamente. Não é thread-safe: quem chama deve sincronizar por instância.
 *
 * Três mecanismos:
 *  1. Limites fixos de temperatura (alerta/crítico) com histerese.
 *  2. Limites fixos de corrente (alerta/crítico).
 *  3. Anomalia estatística da corrente: valor acima de média + k*desvio + margem, por N leituras seguidas,
 *     calculada sobre uma janela de leituras consideradas normais (linha de base).
 */
public class DetectorAnomalia {

    private final LimitesProperties cfg;

    private Nivel nivelTemperatura = Nivel.NORMAL;
    private Nivel nivelCorrente = Nivel.NORMAL;

    private final Deque<Double> linhaDeBase = new ArrayDeque<>();
    private int consecutivasAcimaDoLimite = 0;
    private boolean anomaliaAtiva = false;

    public DetectorAnomalia(LimitesProperties cfg) {
        this.cfg = cfg;
    }

    /** Avalia uma nova leitura e devolve os alertas disparados por ela (lista vazia se nenhum). */
    public List<EventoAlerta> avaliar(double temperaturaC, double correnteA) {
        List<EventoAlerta> eventos = new ArrayList<>();
        avaliarTemperatura(temperaturaC, eventos);
        avaliarCorrente(correnteA, eventos);
        avaliarEstatistica(correnteA, eventos);
        return eventos;
    }

    private void avaliarTemperatura(double temp, List<EventoAlerta> eventos) {
        var t = cfg.temperatura();
        Nivel novo = proximoNivel(nivelTemperatura, temp, t.alerta(), t.critico(), t.histerese());
        if (novo.ordinal() > nivelTemperatura.ordinal()) {
            if (novo == Nivel.CRITICO) {
                eventos.add(new EventoAlerta(TipoAlerta.TEMPERATURA_CRITICA, temp,
                        msg("Temperatura de %.1f °C atingiu o limite crítico (%.1f °C)", temp, t.critico())));
            } else {
                eventos.add(new EventoAlerta(TipoAlerta.TEMPERATURA_ALERTA, temp,
                        msg("Temperatura de %.1f °C atingiu o limite de alerta (%.1f °C)", temp, t.alerta())));
            }
        }
        nivelTemperatura = novo;
    }

    private void avaliarCorrente(double corrente, List<EventoAlerta> eventos) {
        var c = cfg.corrente();
        Nivel novo = proximoNivel(nivelCorrente, corrente, c.alerta(), c.critico(), c.histerese());
        if (novo.ordinal() > nivelCorrente.ordinal()) {
            if (novo == Nivel.CRITICO) {
                eventos.add(new EventoAlerta(TipoAlerta.CORRENTE_CRITICA, corrente,
                        msg("Corrente de %.2f A atingiu o limite crítico (%.2f A)", corrente, c.critico())));
            } else {
                eventos.add(new EventoAlerta(TipoAlerta.CORRENTE_ALERTA, corrente,
                        msg("Corrente de %.2f A atingiu o limite de alerta (%.2f A)", corrente, c.alerta())));
            }
        }
        nivelCorrente = novo;
    }

    private void avaliarEstatistica(double corrente, List<EventoAlerta> eventos) {
        var e = cfg.estatistica();
        if (linhaDeBase.size() >= e.minAmostras()) {
            double media = linhaDeBase.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double variancia = linhaDeBase.stream()
                    .mapToDouble(x -> (x - media) * (x - media)).sum() / linhaDeBase.size();
            double limite = media + e.sigmas() * Math.sqrt(variancia) + e.margem();

            if (corrente > limite) {
                consecutivasAcimaDoLimite++;
                if (consecutivasAcimaDoLimite >= e.consecutivas() && !anomaliaAtiva) {
                    anomaliaAtiva = true;
                    eventos.add(new EventoAlerta(TipoAlerta.ANOMALIA_ESTATISTICA, corrente,
                            msg("Corrente de %.2f A acima do padrão (limite estatístico %.2f A) por %d leituras seguidas",
                                    corrente, limite, consecutivasAcimaDoLimite)));
                }
                return; // leitura anômala não entra na linha de base
            }
            consecutivasAcimaDoLimite = 0;
            anomaliaAtiva = false;
        }
        linhaDeBase.addLast(corrente);
        if (linhaDeBase.size() > e.janela()) {
            linhaDeBase.removeFirst();
        }
    }

    /**
     * Histerese: sobe de nível assim que o valor cruza o limite, mas só desce depois que o valor
     * cai abaixo de (limite do nível atual - histerese).
     */
    static Nivel proximoNivel(Nivel atual, double valor, double alerta, double critico, double histerese) {
        Nivel bruto = valor >= critico ? Nivel.CRITICO : valor >= alerta ? Nivel.ALERTA : Nivel.NORMAL;
        if (bruto.ordinal() >= atual.ordinal()) {
            return bruto;
        }
        double limiteAtual = atual == Nivel.CRITICO ? critico : alerta;
        return valor < limiteAtual - histerese ? bruto : atual;
    }

    public Nivel getNivelTemperatura() { return nivelTemperatura; }
    public Nivel getNivelCorrente() { return nivelCorrente; }
    public boolean isAnomaliaAtiva() { return anomaliaAtiva; }

    private static String msg(String formato, Object... args) {
        return String.format(Locale.of("pt", "BR"), formato, args);
    }
}
