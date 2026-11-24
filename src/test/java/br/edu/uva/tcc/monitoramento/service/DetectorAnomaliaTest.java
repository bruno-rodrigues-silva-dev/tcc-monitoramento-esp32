package br.edu.uva.tcc.monitoramento.service;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.uva.tcc.monitoramento.config.LimitesProperties;
import br.edu.uva.tcc.monitoramento.domain.TipoAlerta;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DetectorAnomaliaTest {

    private DetectorAnomalia detector;

    @BeforeEach
    void setUp() {
        var cfg = new LimitesProperties(
                new LimitesProperties.Temperatura(50.0, 60.0, 2.0),
                new LimitesProperties.Corrente(1.6, 2.0, 0.0),
                new LimitesProperties.Estatistica(3.0, 0.05, 3, 120, 30),
                Duration.ofSeconds(15));
        detector = new DetectorAnomalia(cfg);
    }

    private static List<TipoAlerta> tipos(List<EventoAlerta> eventos) {
        return eventos.stream().map(EventoAlerta::tipo).toList();
    }

    @Test
    void operacaoNormalNaoGeraAlerta() {
        for (int i = 0; i < 100; i++) {
            assertThat(detector.avaliar(30.0, i % 2 == 0 ? 0.98 : 1.02)).isEmpty();
        }
        assertThat(detector.getNivelTemperatura()).isEqualTo(Nivel.NORMAL);
    }

    @Test
    void temperaturaGeraAlertaEmCinquentaEHisteresePrendeOEstado() {
        assertThat(detector.avaliar(49.9, 1.0)).isEmpty();
        assertThat(tipos(detector.avaliar(50.0, 1.0))).containsExactly(TipoAlerta.TEMPERATURA_ALERTA);
        assertThat(detector.avaliar(51.0, 1.0)).isEmpty();      // continua em alerta, não repete
        assertThat(detector.avaliar(49.0, 1.0)).isEmpty();      // dentro da histerese (>= 48)
        assertThat(detector.getNivelTemperatura()).isEqualTo(Nivel.ALERTA);
        assertThat(detector.avaliar(47.9, 1.0)).isEmpty();      // saiu da histerese
        assertThat(detector.getNivelTemperatura()).isEqualTo(Nivel.NORMAL);
        assertThat(tipos(detector.avaliar(50.5, 1.0))).containsExactly(TipoAlerta.TEMPERATURA_ALERTA); // rearmou
    }

    @Test
    void temperaturaCriticaEmSessenta() {
        detector.avaliar(55.0, 1.0);
        assertThat(tipos(detector.avaliar(60.0, 1.0))).containsExactly(TipoAlerta.TEMPERATURA_CRITICA);
        assertThat(detector.avaliar(58.5, 1.0)).isEmpty();      // histerese do crítico (>= 58)
        assertThat(detector.getNivelTemperatura()).isEqualTo(Nivel.CRITICO);
    }

    @Test
    void correnteGeraAlertaEDepoisCritico() {
        assertThat(tipos(detector.avaliar(30.0, 1.7))).containsExactly(TipoAlerta.CORRENTE_ALERTA);
        assertThat(tipos(detector.avaliar(30.0, 2.1))).containsExactly(TipoAlerta.CORRENTE_CRITICA);
    }

    @Test
    void anomaliaEstatisticaExigeTresLeiturasSeguidasEUmaSoVez() {
        for (int i = 0; i < 30; i++) {                          // linha de base: ~1,0 A
            assertThat(detector.avaliar(30.0, i % 2 == 0 ? 0.98 : 1.02)).isEmpty();
        }
        assertThat(detector.avaliar(30.0, 1.5)).isEmpty();      // 1ª acima do limite estatístico
        assertThat(detector.avaliar(30.0, 1.5)).isEmpty();      // 2ª
        assertThat(tipos(detector.avaliar(30.0, 1.5))).containsExactly(TipoAlerta.ANOMALIA_ESTATISTICA); // 3ª
        assertThat(detector.avaliar(30.0, 1.5)).isEmpty();      // não repete
        assertThat(detector.isAnomaliaAtiva()).isTrue();

        detector.avaliar(30.0, 1.0);                            // volta ao normal
        assertThat(detector.isAnomaliaAtiva()).isFalse();
    }

    @Test
    void picoIsoladoNaoGeraAnomalia() {
        for (int i = 0; i < 30; i++) {
            detector.avaliar(30.0, i % 2 == 0 ? 0.98 : 1.02);
        }
        assertThat(detector.avaliar(30.0, 1.5)).isEmpty();
        assertThat(detector.avaliar(30.0, 1.0)).isEmpty();      // contador zera
        assertThat(detector.avaliar(30.0, 1.5)).isEmpty();
        assertThat(detector.avaliar(30.0, 1.5)).isEmpty();
        assertThat(detector.isAnomaliaAtiva()).isFalse();
    }
}
