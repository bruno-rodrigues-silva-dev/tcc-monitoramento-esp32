package br.edu.uva.tcc.monitoramento.dto;

import br.edu.uva.tcc.monitoramento.domain.Leitura;
import java.time.Instant;

public record LeituraResponse(Long id, String dispositivoId, double temperaturaC, double correnteA, Instant instante) {

    public static LeituraResponse de(Leitura l) {
        return new LeituraResponse(l.getId(), l.getDispositivoId(), l.getTemperaturaC(), l.getCorrenteA(), l.getInstante());
    }
}
