package com.exemplo.financiamento.service;

import java.math.BigDecimal;
import java.util.Random;

import com.exemplo.financiamento.dto.SimulacaoRequest;
import com.exemplo.financiamento.dto.SimulacaoResponse;
import com.exemplo.financiamento.model.Faixa;
import com.exemplo.financiamento.model.TipoRenda;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SimulacaoServiceTests {

	private static final BigDecimal IMOVEL = new BigDecimal("200000.00");

	// Sorteio fixo: taxa e subsídio saem sempre na mesma fração do intervalo.
	private SimulacaoResponse simular(String renda, double sorteio) {
		Random fixo = new Random() {
			@Override
			public double nextDouble() {
				return sorteio;
			}
		};
		SimulacaoService service = new SimulacaoService(new RegrasService(fixo), new CalculoService());
		return service.simular(new SimulacaoRequest("Maria", 30, TipoRenda.PROPRIA, new BigDecimal(renda), IMOVEL));
	}

	@Test
	void semSubsidioAEntradaEh20PorCentoDoImovel() {
		SimulacaoResponse r = simular("6000.00", 0.5);
		assertThat(r.faixa()).isEqualTo(Faixa.FAIXA_3);
		assertThat(r.subsidio()).isEqualByComparingTo("0");
		assertThat(r.valorFinanciado()).isEqualByComparingTo("160000.00");
		assertThat(r.entrada()).isEqualByComparingTo("40000.00");
	}

	@Test
	void subsidioAbateDoPrecoEDiminuiAEntrada() {
		// R$ 200.000 - R$ 32.500 = R$ 167.500 a pagar; a Caixa financia R$ 160.000 (80% do preço).
		SimulacaoResponse r = simular("3000.00", 0.5);
		assertThat(r.subsidio()).isEqualByComparingTo("32500.00");
		assertThat(r.valorFinanciado()).isEqualByComparingTo("160000.00");
		assertThat(r.entrada()).isEqualByComparingTo("7500.00");
	}

	@Test
	void subsidioMaiorQue20PorCentoZeraAEntradaEReduzOFinanciamento() {
		// R$ 200.000 - R$ 58.500 = R$ 141.500 a pagar, menos que os 80% que a Caixa financiaria.
		SimulacaoResponse r = simular("3000.00", 0.9);
		assertThat(r.subsidio()).isEqualByComparingTo("58500.00");
		assertThat(r.entrada()).isEqualByComparingTo("0");
		assertThat(r.valorFinanciado()).isEqualByComparingTo("141500.00");
	}

	@Test
	void entradaSubsidioEFinanciamentoSomamOValorDoImovel() {
		for (double sorteio : new double[] { 0.0, 0.1, 0.3, 0.5, 0.9, 0.999 }) {
			SimulacaoResponse r = simular("4000.00", sorteio);
			assertThat(r.entrada().add(r.subsidio()).add(r.valorFinanciado())).isEqualByComparingTo(IMOVEL);
			assertThat(r.price().parcelas()).hasSize(r.prazoMeses());
			assertThat(r.sac().parcelas().getLast().saldoDevedor()).isEqualByComparingTo("0");
			// Os totais fecham no centavo nos dois sistemas.
			assertThat(r.price().totalPago()).isEqualByComparingTo(r.valorFinanciado().add(r.price().totalJuros()));
			assertThat(r.sac().totalPago()).isEqualByComparingTo(r.valorFinanciado().add(r.sac().totalJuros()));
		}
	}

}
