package com.exemplo.financiamento.service;

import java.math.BigDecimal;

import com.exemplo.financiamento.model.Faixa;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegrasServiceTests {

	private final RegrasService regras = new RegrasService();

	@ParameterizedTest
	@CsvSource({ "18,420", "49,420", "50,330", "54,330", "55,300", "59,300", "60,240", "64,240", "65,180",
			"69,180", "70,120", "85,120" })
	void prazoPorIdade(int idade, int meses) {
		assertThat(regras.prazoMeses(idade)).isEqualTo(meses);
	}

	@Test
	void menorDeIdadeNaoFinancia() {
		assertThatThrownBy(() -> regras.prazoMeses(17)).isInstanceOf(RegraNegocioException.class);
	}

	@ParameterizedTest
	@CsvSource({ "3200.00,FAIXA_1", "3200.01,FAIXA_2", "5000.00,FAIXA_2", "5000.01,FAIXA_3", "9600.00,FAIXA_3",
			"9600.01,FAIXA_4", "13000.00,FAIXA_4" })
	void faixaPorRenda(String renda, Faixa faixa) {
		assertThat(Faixa.de(new BigDecimal(renda))).isEqualTo(faixa);
	}

	@Test
	void rendaAcimaDoTetoFicaForaDoPrograma() {
		assertThatThrownBy(() -> Faixa.de(new BigDecimal("13000.01"))).isInstanceOf(RegraNegocioException.class);
	}

	@Test
	void taxaSorteadaFicaDentroDaFaixa() {
		for (Faixa faixa : Faixa.values()) {
			for (int i = 0; i < 1000; i++) {
				double taxa = regras.sortearTaxaAnual(faixa).doubleValue();
				assertThat(taxa).isBetween(faixa.getTaxaMinima(), faixa.getTaxaMaxima());
			}
		}
	}

	@Test
	void faixa4TemTaxaFixa() {
		assertThat(regras.sortearTaxaAnual(Faixa.FAIXA_4)).isEqualByComparingTo("10.00");
	}

	@Test
	void subsidioSoNasFaixas1e2() {
		BigDecimal saldo = new BigDecimal("160000.00");
		for (int i = 0; i < 1000; i++) {
			assertThat(regras.sortearSubsidio(Faixa.FAIXA_1, saldo)).isBetween(BigDecimal.ZERO,
					new BigDecimal("65000.00"));
			assertThat(regras.sortearSubsidio(Faixa.FAIXA_2, saldo)).isBetween(BigDecimal.ZERO,
					new BigDecimal("65000.00"));
		}
		assertThat(regras.sortearSubsidio(Faixa.FAIXA_3, saldo)).isEqualByComparingTo("0");
		assertThat(regras.sortearSubsidio(Faixa.FAIXA_4, saldo)).isEqualByComparingTo("0");
	}

	@Test
	void subsidioNaoPassaDoSaldoAFinanciar() {
		BigDecimal saldo = new BigDecimal("100.00");
		for (int i = 0; i < 1000; i++) {
			assertThat(regras.sortearSubsidio(Faixa.FAIXA_1, saldo)).isLessThanOrEqualTo(saldo);
		}
	}

	@Test
	void bancoFinanciaAte80PorCentoDoImovel() {
		assertThat(regras.financiamentoMaximo(new BigDecimal("200000.00"))).isEqualByComparingTo("160000.00");
	}

}
