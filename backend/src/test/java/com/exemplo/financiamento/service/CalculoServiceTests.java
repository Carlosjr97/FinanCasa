package com.exemplo.financiamento.service;

import java.math.BigDecimal;
import java.util.List;

import com.exemplo.financiamento.dto.Parcela;
import com.exemplo.financiamento.dto.TabelaAmortizacao;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class CalculoServiceTests {

	private static final BigDecimal FINANCIADO = new BigDecimal("130000.00");

	private static final int PRAZO = 420;

	// Arredondar a parcela fixa em centavos desvia o saldo aos poucos; em 420 meses a 5% ao ano
	// o acerto na última parcela não passa de alguns reais.
	private static final double AJUSTE_ULTIMA_PARCELA = 6.00;

	private final CalculoService calculo = new CalculoService();

	private final double taxaMensal = calculo.taxaMensal(new BigDecimal("5.00"));

	@Test
	void taxaMensalEquivalente() {
		assertThat(taxaMensal * 100).isCloseTo(0.4074, within(0.0001));
	}

	@Test
	void priceTemParcelaFixa() {
		TabelaAmortizacao tabela = calculo.price(FINANCIADO, taxaMensal, PRAZO);

		assertThat(tabela.parcelas()).hasSize(PRAZO);
		assertThat(tabela.primeiraParcela()).isEqualByComparingTo("646.92");
		// Todas as parcelas são iguais, menos a última, que absorve o arredondamento acumulado.
		for (int k = 0; k < PRAZO - 1; k++) {
			assertThat(tabela.parcelas().get(k).valor()).isEqualByComparingTo("646.92");
		}
		assertThat(tabela.ultimaParcela().doubleValue()).isCloseTo(646.92, within(AJUSTE_ULTIMA_PARCELA));
		assertThat(tabela.parcelas().getLast().saldoDevedor()).isEqualByComparingTo("0");
		assertThat(somaAmortizacoes(tabela.parcelas())).isEqualByComparingTo(FINANCIADO);
		assertThat(tabela.totalPago()).isEqualByComparingTo(FINANCIADO.add(tabela.totalJuros()));
	}

	@Test
	void sacTemParcelaDecrescente() {
		TabelaAmortizacao tabela = calculo.sac(FINANCIADO, taxaMensal, PRAZO);

		assertThat(tabela.parcelas()).hasSize(PRAZO);
		assertThat(tabela.primeiraParcela()).isEqualByComparingTo("839.16");
		assertThat(tabela.ultimaParcela()).isEqualByComparingTo("310.78");
		assertThat(tabela.parcelas().getLast().saldoDevedor()).isEqualByComparingTo("0");
		assertThat(somaAmortizacoes(tabela.parcelas())).isEqualByComparingTo(FINANCIADO);
		assertThat(tabela.totalPago()).isEqualByComparingTo(FINANCIADO.add(tabela.totalJuros()));
		for (int k = 1; k < PRAZO; k++) {
			assertThat(tabela.parcelas().get(k).valor()).isLessThanOrEqualTo(tabela.parcelas().get(k - 1).valor());
		}
	}

	@Test
	void sacPagaMenosJurosQuePrice() {
		BigDecimal jurosPrice = calculo.price(FINANCIADO, taxaMensal, PRAZO).totalJuros();
		BigDecimal jurosSac = calculo.sac(FINANCIADO, taxaMensal, PRAZO).totalJuros();

		assertThat(jurosSac).isLessThan(jurosPrice);
	}

	private BigDecimal somaAmortizacoes(List<Parcela> parcelas) {
		return parcelas.stream().map(Parcela::amortizacao).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

}
