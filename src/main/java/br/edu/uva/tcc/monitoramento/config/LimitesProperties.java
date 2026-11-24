package br.edu.uva.tcc.monitoramento.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Parâmetros de limites e detecção, lidos do application.yml (prefixo "monitoramento"). */
@ConfigurationProperties(prefix = "monitoramento")
public record LimitesProperties(
        Temperatura temperatura,
        Corrente corrente,
        Estatistica estatistica,
        Duration timeoutComunicacao) {

    /** Limites de temperatura em °C, com histerese para evitar alertas repetidos. */
    public record Temperatura(double alerta, double critico, double histerese) {}

    /** Limites de corrente RMS em A. */
    public record Corrente(double alerta, double critico, double histerese) {}

    /** Detecção estatística: média + sigmas * desvio-padrão + margem, por N leituras consecutivas. */
    public record Estatistica(double sigmas, double margem, int consecutivas, int janela, int minAmostras) {}
}
