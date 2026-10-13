import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { Moeda } from '../../directives/moeda';
import { SimulacaoRequest, TipoRenda } from '../../models/simulacao';
import { SimulacaoService } from '../../services/simulacao.service';

@Component({
  selector: 'app-identificacao',
  imports: [ReactiveFormsModule, Moeda],
  templateUrl: './identificacao.html',
  styleUrl: './identificacao.scss',
})
export class Identificacao {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(SimulacaoService);
  private readonly router = inject(Router);

  protected readonly form = this.fb.group({
    nome: ['', Validators.required],
    idade: [null as number | null, [Validators.required, Validators.min(18), Validators.pattern(/^\d+$/)]],
    tipoRenda: ['PROPRIA' as TipoRenda, Validators.required],
    renda: [null as number | null, [Validators.required, Validators.min(0.01)]],
    valorImovel: [null as number | null, [Validators.required, Validators.min(0.01)]],
  });

  constructor() {
    // Voltando da tela de modelo, repreenche o formulário com o que já foi informado.
    const dados = this.service.dados();
    if (dados) {
      this.form.patchValue(dados);
    }
  }

  protected invalido(campo: keyof typeof this.form.controls): boolean {
    const controle = this.form.controls[campo];
    return controle.invalid && controle.touched;
  }

  protected enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.service.dados.set(this.form.getRawValue() as SimulacaoRequest);
    this.router.navigate(['/modelo']);
  }
}
