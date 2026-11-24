package br.edu.uva.tcc.monitoramento.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Corpo enviado pelo ESP32. Faixas: DS18B20 (-55 a 125 °C) e ACS712 5 A (0 a 5 A RMS). */
public record LeituraRequest(
        @NotBlank @Size(max = 64) String dispositivoId,
        @NotNull @DecimalMin("-55.0") @DecimalMax("125.0") Double temperaturaC,
        @NotNull @DecimalMin("0.0") @DecimalMax("5.0") Double correnteA) {}
