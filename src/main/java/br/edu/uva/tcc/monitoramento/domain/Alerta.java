package br.edu.uva.tcc.monitoramento.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "alertas", indexes = @Index(name = "idx_alerta_disp_instante", columnList = "dispositivoId, instante"))
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String dispositivoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TipoAlerta tipo;

    /** Valor que disparou o alerta (°C ou A); nulo em falha de comunicação. */
    private Double valor;

    @Column(nullable = false, length = 255)
    private String mensagem;

    @Column(nullable = false)
    private Instant instante;

    protected Alerta() {}

    public Alerta(String dispositivoId, TipoAlerta tipo, Double valor, String mensagem, Instant instante) {
        this.dispositivoId = dispositivoId;
        this.tipo = tipo;
        this.valor = valor;
        this.mensagem = mensagem;
        this.instante = instante;
    }

    public Long getId() { return id; }
    public String getDispositivoId() { return dispositivoId; }
    public TipoAlerta getTipo() { return tipo; }
    public Double getValor() { return valor; }
    public String getMensagem() { return mensagem; }
    public Instant getInstante() { return instante; }
}
