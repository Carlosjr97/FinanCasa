import { Directive, ElementRef, forwardRef, inject } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

// Máscara de moeda: a pessoa digita só os números e o campo mostra 1.234,56.
// O formulário recebe o valor como número (1234.56), ou null se o campo estiver vazio.
@Directive({
  selector: 'input[appMoeda]',
  host: { '(input)': 'aoDigitar()', '(blur)': 'aoTocar()' },
  providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => Moeda), multi: true }],
})
export class Moeda implements ControlValueAccessor {
  private readonly input = inject<ElementRef<HTMLInputElement>>(ElementRef).nativeElement;
  private readonly formato = new Intl.NumberFormat('pt-BR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });

  private aoMudar: (valor: number | null) => void = () => {};
  protected aoTocar: () => void = () => {};

  protected aoDigitar(): void {
    // Ignora tudo que não é dígito; os dois últimos dígitos são os centavos.
    const centavos = Number(this.input.value.replace(/\D/g, '').slice(0, 13));
    const valor = centavos > 0 ? centavos / 100 : null;
    this.writeValue(valor);
    this.aoMudar(valor);
  }

  writeValue(valor: number | null): void {
    this.input.value = valor == null ? '' : this.formato.format(valor);
  }

  registerOnChange(fn: (valor: number | null) => void): void {
    this.aoMudar = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.aoTocar = fn;
  }

  setDisabledState(desabilitado: boolean): void {
    this.input.disabled = desabilitado;
  }
}
