package com.exemplo.financiamento.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.exemplo.financiamento.dto.SimulacaoRequest;
import com.exemplo.financiamento.dto.SimulacaoResponse;
import com.exemplo.financiamento.model.Faixa;

import org.springframework.stereotype.Service;

@Service
public class SimulacaoService {

	private final RegrasService regras;

	private final CalculoService calculo;

	public SimulacaoService(RegrasService regras, CalculoService calculo) {
		this.regras = regras;
		this.calculo = calculo;
	}

	public SimulacaoResponse simular(SimulacaoRequest req) {
		int prazo = regras.prazoMeses(req.idade());
		Faixa faixa = Faixa.de(req.renda());
		BigDecimal taxaAnual = regras.sortearTaxaAnual(faixa);
		BigDecimal subsidio = regras.sortearSubsidio(faixa, req.valorImovel());
		BigDecimal saldoAposSubsidio = req.valorImovel().subtract(subsidio).setScale(2, RoundingMode.HALF_UP);
		// O subsídio abate do preço do imóvel e a Caixa financia até 80% do preço original.
		// Por isso o subsídio acaba diminuindo a entrada, podendo até zerá-la.
		BigDecimal financiado = regras.financiamentoMaximo(req.valorImovel()).min(saldoAposSubsidio);
		// A entrada é o que sobra do imóvel depois do subsídio e do financiamento.
		BigDecimal entrada = saldoAposSubsidio.subtract(financiado);
		double taxaMensal = calculo.taxaMensal(taxaAnual);

		return new SimulacaoResponse(req.nome(), faixa, prazo, taxaAnual,
				BigDecimal.valueOf(taxaMensal * 100).setScale(4, RoundingMode.HALF_UP), subsidio, entrada, financiado,
				calculo.price(financiado, taxaMensal, prazo), calculo.sac(financiado, taxaMensal, prazo));
	}

}
