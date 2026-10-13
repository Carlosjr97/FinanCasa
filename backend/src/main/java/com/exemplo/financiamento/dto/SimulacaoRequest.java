package com.exemplo.financiamento.dto;

import java.math.BigDecimal;

import com.exemplo.financiamento.model.TipoRenda;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SimulacaoRequest(
		@NotBlank String nome,
		@NotNull @Min(18) Integer idade,
		@NotNull TipoRenda tipoRenda,
		@NotNull @Positive BigDecimal renda,
		@NotNull @Positive BigDecimal valorImovel) {
}
