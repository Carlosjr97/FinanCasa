package com.exemplo.financiamento.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.exemplo.financiamento.dto.Parcela;
import com.exemplo.financiamento.dto.TabelaAmortizacao;

import org.springframework.stereotype.Service;

@Service
public class CalculoService {

	// Taxa mensal equivalente por juros compostos, em decimal (0.004074 = 0,4074%).
	public double taxaMensal(BigDecimal taxaAnualPercentual) {
		return Math.pow(1 + taxaAnualPercentual.doubleValue() / 100, 1.0 / 12) - 1;
	}

	// As contas são feitas em centavos, linha a linha, como em um contrato: assim a soma das
	// amortizações dá exatamente o valor financiado e o total pago fecha com financiado + juros.
	public TabelaAmortizacao price(BigDecimal valorFinanciado, double i, int n) {
		BigDecimal saldo = valorFinanciado.setScale(2, RoundingMode.HALF_UP);
		BigDecimal parcelaFixa = arredondar(saldo.doubleValue() * i / (1 - Math.pow(1 + i, -n)));
		List<Parcela> parcelas = new ArrayList<>();
		for (int k = 1; k <= n; k++) {
			BigDecimal juros = arredondar(saldo.doubleValue() * i);
			// A última parcela quita o que sobrou do saldo: é nela que cai a diferença acumulada
			// por arredondar a parcela fixa em centavos.
			BigDecimal amortizacao = (k == n) ? saldo : parcelaFixa.subtract(juros).min(saldo);
			saldo = saldo.subtract(amortizacao);
			parcelas.add(new Parcela(k, amortizacao.add(juros), juros, amortizacao, saldo));
		}
		return resumo(parcelas);
	}

	public TabelaAmortizacao sac(BigDecimal valorFinanciado, double i, int n) {
		BigDecimal total = valorFinanciado.setScale(2, RoundingMode.HALF_UP);
		BigDecimal saldo = total;
		List<Parcela> parcelas = new ArrayList<>();
		for (int k = 1; k <= n; k++) {
			BigDecimal juros = arredondar(saldo.doubleValue() * i);
			// O saldo desce em n degraus iguais. Arredondar o saldo, e não a amortização, espalha
			// os centavos pelas parcelas em vez de acumular tudo na última.
			BigDecimal novoSaldo = total.multiply(BigDecimal.valueOf(n - k))
				.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
			BigDecimal amortizacao = saldo.subtract(novoSaldo);
			saldo = novoSaldo;
			parcelas.add(new Parcela(k, amortizacao.add(juros), juros, amortizacao, saldo));
		}
		return resumo(parcelas);
	}

	private TabelaAmortizacao resumo(List<Parcela> parcelas) {
		BigDecimal totalJuros = parcelas.stream().map(Parcela::juros).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal totalPago = parcelas.stream().map(Parcela::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
		return new TabelaAmortizacao(parcelas.getFirst().valor(), parcelas.getLast().valor(), totalJuros,
				totalPago, parcelas);
	}

	private BigDecimal arredondar(double valor) {
		return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP);
	}

}
