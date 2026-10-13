package com.exemplo.financiamento.dto;

import java.math.BigDecimal;

public record Parcela(int numero, BigDecimal valor, BigDecimal juros, BigDecimal amortizacao,
		BigDecimal saldoDevedor) {
}
