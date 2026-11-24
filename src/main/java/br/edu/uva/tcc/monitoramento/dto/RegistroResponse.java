package br.edu.uva.tcc.monitoramento.dto;

import java.util.List;

/** Resposta do POST /api/leituras: a leitura gravada e os alertas disparados por ela. */
public record RegistroResponse(LeituraResponse leitura, List<AlertaResponse> alertasGerados) {}
