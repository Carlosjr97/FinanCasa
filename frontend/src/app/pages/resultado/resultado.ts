import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { Faixa, Sistema } from '../../models/simulacao';
import { SimulacaoService } from '../../services/simulacao.service';

const NOME_FAIXA: Record<Faixa, string> = {
  FAIXA_1: 'Faixa 1',
  FAIXA_2: 'Faixa 2',
  FAIXA_3: 'Faixa 3',
  FAIXA_4: 'Faixa 4',
};

@Component({
  selector: 'app-resultado',
  imports: [CurrencyPipe, DecimalPipe, RouterLink],
  templateUrl: './resultado.html',
  styleUrl: './resultado.scss',
})
export class Resultado {
  private readonly service = inject(SimulacaoService);
  private readonly router = inject(Router);

  protected readonly resultado = this.service.resultado;
  protected readonly sistema = signal<Sistema>('price');
  protected readonly tabela = computed(() => this.resultado()?.[this.sistema()] ?? null);
  protected readonly nomeFaixa = computed(() => {
    const faixa = this.resultado()?.faixa;
    return faixa ? NOME_FAIXA[faixa] : '';
  });

  constructor() {
    // Sem simulação em memória (ex.: página recarregada), volta para o formulário.
    if (!this.resultado()) {
      this.router.navigate(['/']);
    }
  }
}
