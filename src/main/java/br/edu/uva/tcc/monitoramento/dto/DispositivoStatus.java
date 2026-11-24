package br.edu.uva.tcc.monitoramento.dto;

import java.time.Instant;

public record DispositivoStatus(
        String dispositivoId,
        Instant ultimaLeitura,
        boolean comunicacaoOk,
        String nivelTemperatura,
        String nivelCorrente,
        boolean anomaliaEstatisticaAtiva,
        double ultimaTemperaturaC,
        double ultimaCorrenteA) {}
