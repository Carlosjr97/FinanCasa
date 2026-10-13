package com.exemplo.financiamento.model;

import java.math.BigDecimal;
import java.util.Arrays;

import com.exemplo.financiamento.service.RegraNegocioException;

public enum Faixa {

	FAIXA_1("3200.00", 4.00, 5.00, true, "275000.00"),
	FAIXA_2("5000.00", 4.75, 7.00, true, "275000.00"),
	FAIXA_3("9600.00", 7.66, 8.16, false, "400000.00"),
	FAIXA_4("13000.00", 10.00, 10.00, false, "600000.00");

	private final BigDecimal rendaMaxima;

	private final double taxaMinima;

	private final double taxaMaxima;

	private final boolean temSubsidio;

	private final BigDecimal valorMaximoImovel;

	Faixa(String rendaMaxima, double taxaMinima, double taxaMaxima, boolean temSubsidio, String valorMaximoImovel) {
		this.rendaMaxima = new BigDecimal(rendaMaxima);
		this.taxaMinima = taxaMinima;
		this.taxaMaxima = taxaMaxima;
		this.temSubsidio = temSubsidio;
		this.valorMaximoImovel = new BigDecimal(valorMaximoImovel);
	}

	// O valor exato do limite fica na faixa de baixo (R$ 3.200,00 é faixa 1).
	public static Faixa de(BigDecimal renda) {
		return Arrays.stream(values())
			.filter(faixa -> renda.compareTo(faixa.rendaMaxima) <= 0)
			.findFirst()
			.orElseThrow(() -> new RegraNegocioException(
					"Renda acima de R$ 13.000,00 está fora das faixas do programa"));
	}

	public BigDecimal getRendaMaxima() {
		return rendaMaxima;
	}

	public double getTaxaMinima() {
		return taxaMinima;
	}

	public double getTaxaMaxima() {
		return taxaMaxima;
	}

	public boolean isTemSubsidio() {
		return temSubsidio;
	}

	public BigDecimal getValorMaximoImovel() {
		return valorMaximoImovel;
	}

}
