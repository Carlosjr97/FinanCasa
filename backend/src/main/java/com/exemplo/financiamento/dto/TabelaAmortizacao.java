package com.exemplo.financiamento.dto;

import java.math.BigDecimal;
import java.util.List;

public record TabelaAmortizacao(BigDecimal primeiraParcela, BigDecimal ultimaParcela, BigDecimal totalJuros,
		BigDecimal totalPago, List<Parcela> parcelas) {
}
