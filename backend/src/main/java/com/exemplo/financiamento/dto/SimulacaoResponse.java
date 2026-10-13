package com.exemplo.financiamento.dto;

import java.math.BigDecimal;

import com.exemplo.financiamento.model.Faixa;

public record SimulacaoResponse(String nome, Faixa faixa, int prazoMeses, BigDecimal taxaAnual,
		BigDecimal taxaMensal, BigDecimal subsidio, BigDecimal entrada, BigDecimal valorFinanciado,
		TabelaAmortizacao price, TabelaAmortizacao sac) {
}
