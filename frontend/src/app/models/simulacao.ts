export type TipoRenda = 'PROPRIA' | 'FAMILIAR';
export type Sistema = 'price' | 'sac';
export type Faixa = 'FAIXA_1' | 'FAIXA_2' | 'FAIXA_3' | 'FAIXA_4';

// O que o usuário preenche na tela de identificação e vai para o backend.
export interface SimulacaoRequest {
  nome: string;
  idade: number;
  tipoRenda: TipoRenda;
  renda: number;
  valorImovel: number;
}

export interface Parcela {
  numero: number;
  valor: number;
  juros: number;
  amortizacao: number;
  saldoDevedor: number;
}

export interface TabelaAmortizacao {
  primeiraParcela: number;
  ultimaParcela: number;
  totalJuros: number;
  totalPago: number;
  parcelas: Parcela[];
}

export interface SimulacaoResponse {
  nome: string;
  faixa: Faixa;
  prazoMeses: number;
  taxaAnual: number;
  taxaMensal: number;
  subsidio: number;
  entrada: number;
  valorFinanciado: number;
  price: TabelaAmortizacao;
  sac: TabelaAmortizacao;
}
