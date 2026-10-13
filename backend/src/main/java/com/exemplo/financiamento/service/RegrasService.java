package com.exemplo.financiamento.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import com.exemplo.financiamento.model.Faixa;

import org.springframework.stereotype.Service;

@Service
public class RegrasService {

	private static final double SUBSIDIO_MAXIMO = 65_000.00;

	private static final BigDecimal COTA_MAXIMA_FINANCIAMENTO = new BigDecimal("0.80");

	private final Random random;

	public RegrasService() {
		this(new Random());
	}

	// Permite fixar o sorteio nos testes.
	RegrasService(Random random) {
		this.random = random;
	}

	public int prazoMeses(int idade) {
		if (idade < 18) {
			throw new RegraNegocioException("Idade mínima é 18 anos");
		}
		if (idade <= 49) {
			return 420;
		}
		if (idade <= 54) {
			return 330;
		}
		if (idade <= 59) {
			return 300;
		}
		if (idade <= 64) {
			return 240;
		}
		if (idade <= 69) {
			return 180;
		}
		return 120;
	}

	public BigDecimal sortearTaxaAnual(Faixa faixa) {
		double taxa = faixa.getTaxaMinima()
				+ random.nextDouble() * (faixa.getTaxaMaxima() - faixa.getTaxaMinima());
		return BigDecimal.valueOf(taxa).setScale(2, RoundingMode.HALF_UP);
	}

	public BigDecimal sortearSubsidio(Faixa faixa, BigDecimal valorImovel) {
		if (!faixa.isTemSubsidio()) {
			return BigDecimal.ZERO.setScale(2);
		}
		BigDecimal sorteado = BigDecimal.valueOf(random.nextDouble() * SUBSIDIO_MAXIMO)
			.setScale(2, RoundingMode.HALF_UP);
		return sorteado.min(valorImovel);
	}

	// A Caixa financia no máximo 80% do valor original do imóvel.
	public BigDecimal financiamentoMaximo(BigDecimal valorImovel) {
		return valorImovel.multiply(COTA_MAXIMA_FINANCIAMENTO).setScale(2, RoundingMode.HALF_UP);
	}

}
