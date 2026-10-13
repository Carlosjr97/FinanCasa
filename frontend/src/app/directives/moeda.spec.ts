import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, ReactiveFormsModule } from '@angular/forms';

import { Moeda } from './moeda';

@Component({
  imports: [ReactiveFormsModule, Moeda],
  template: `<input type="text" appMoeda [formControl]="controle" />`,
})
class Hospedeiro {
  readonly controle = new FormControl<number | null>(null);
}

describe('Moeda', () => {
  let fixture: ComponentFixture<Hospedeiro>;
  let input: HTMLInputElement;

  // Os espaços do Intl não importam para o teste.
  const texto = () => input.value.replace(/\s/g, '');

  const digitar = (valor: string) => {
    input.value = valor;
    input.dispatchEvent(new Event('input'));
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [Hospedeiro] }).compileComponents();
    fixture = TestBed.createComponent(Hospedeiro);
    fixture.detectChanges();
    input = fixture.nativeElement.querySelector('input');
  });

  it('formata o que é digitado e guarda um número no formulário', () => {
    digitar('20000000');
    expect(texto()).toBe('200.000,00');
    expect(fixture.componentInstance.controle.value).toBe(200000);
  });

  it('ignora letras e símbolos', () => {
    digitar('R$ 1a2b3,45-e');
    expect(texto()).toBe('123,45');
    expect(fixture.componentInstance.controle.value).toBe(123.45);
  });

  it('campo vazio ou zerado vira null', () => {
    digitar('0,00');
    expect(texto()).toBe('');
    expect(fixture.componentInstance.controle.value).toBeNull();
  });

  it('mostra formatado um valor que já veio do formulário', () => {
    fixture.componentInstance.controle.setValue(3000.5);
    expect(texto()).toBe('3.000,50');
  });
});