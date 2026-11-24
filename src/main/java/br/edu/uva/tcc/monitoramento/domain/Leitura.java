package br.edu.uva.tcc.monitoramento.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "leituras", indexes = @Index(name = "idx_leitura_disp_instante", columnList = "dispositivoId, instante"))
public class Leitura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String dispositivoId;

    @Column(nullable = false)
    private double temperaturaC;

    @Column(nullable = false)
    private double correnteA;

    @Column(nullable = false)
    private Instant instante;

    protected Leitura() {}

    public Leitura(String dispositivoId, double temperaturaC, double correnteA, Instant instante) {
        this.dispositivoId = dispositivoId;
        this.temperaturaC = temperaturaC;
        this.correnteA = correnteA;
        this.instante = instante;
    }

    public Long getId() { return id; }
    public String getDispositivoId() { return dispositivoId; }
    public double getTemperaturaC() { return temperaturaC; }
    public double getCorrenteA() { return correnteA; }
    public Instant getInstante() { return instante; }
}
