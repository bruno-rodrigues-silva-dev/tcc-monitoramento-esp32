package br.edu.uva.tcc.monitoramento.dto;

import br.edu.uva.tcc.monitoramento.domain.Alerta;
import br.edu.uva.tcc.monitoramento.domain.TipoAlerta;
import java.time.Instant;

public record AlertaResponse(Long id, String dispositivoId, TipoAlerta tipo, Double valor, String mensagem, Instant instante) {

    public static AlertaResponse de(Alerta a) {
        return new AlertaResponse(a.getId(), a.getDispositivoId(), a.getTipo(), a.getValor(), a.getMensagem(), a.getInstante());
    }
}
