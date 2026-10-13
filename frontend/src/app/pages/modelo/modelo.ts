import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { SimulacaoService } from '../../services/simulacao.service';

@Component({
  selector: 'app-modelo',
  imports: [RouterLink],
  templateUrl: './modelo.html',
  styleUrl: './modelo.scss',
})
export class Modelo {
  private readonly service = inject(SimulacaoService);
  private readonly router = inject(Router);

  protected readonly erro = signal<string | null>(null);
  protected readonly carregando = signal(false);

  constructor() {
    //Sem dados de identificação (ex.: página recarregada), volta para o formulário.
    if (!this.service.dados()) {
      this.router.navigate(['/simular']);
    }
  }
  protected escolher(): void {
    const dados = this.service.dados();
    if (!dados) return;
    this.carregando.set(true);
    this.erro.set(null);
    this.service.simular(dados).subscribe({
      next: () => this.router.navigate(['/resultado']),
      error: (e) => {
        this.erro.set(e.error?.mensagem ?? 'Não foi possível simular. Tente de novo.');
        this.carregando.set(false);
      },
    });
  }
}
