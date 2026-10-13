import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { SimulacaoService } from '../../services/simulacao.service';
import { Modelo } from './modelo';

describe('Modelo', () => {
  let component: Modelo;
  let fixture: ComponentFixture<Modelo>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Modelo],
      providers: [provideRouter([]), provideHttpClient()],
    }).compileComponents();

    // Sem dados de identificação o componente redireciona para /simular.
    TestBed.inject(SimulacaoService).dados.set({
      nome: 'Maria',
      idade: 30,
      tipoRenda: 'PROPRIA',
      renda: 3000,
      valorImovel: 200000,    });

    fixture = TestBed.createComponent(Modelo);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
