package br.edu.uva.tcc.monitoramento.service;

import br.edu.uva.tcc.monitoramento.domain.TipoAlerta;

/** Evento produzido pelo detector (ainda não persistido). */
public record EventoAlerta(TipoAlerta tipo, Double valor, String mensagem) {}
