import { HttpClient } from '@angular/common/http'; // traz o HttpClient, a ferramenta do Angular para fazer requisições HTTP (GET, POST etc).
import { Injectable, inject, signal } from '@angular/core'; // injectable marca a classe como serviço que pode ser usado em outros lugares. Inject função para pedir uma depêndencia (aqui, o HttpClient). Signal: cria uma "caixinha" que guarda um valor e avisa a tela automaticamente quando ele muda.
import { tap } from 'rxjs'; // Traz o tap, que permite "espiar" a resposta de uma requisição e fazer algo com ela sem alterá-la.

import { SimulacaoRequest, SimulacaoResponse } from '../models/simulacao'; // Traz os dois tipos que descrevem o formato dos dados: SimulacaoRequest é o que você envia e SimulacaoResponse é o que o backend devolve.

@Injectable({ providedIn: 'root' }) // Diz ao Angular que existe uma única instância desse serviço para o app inteiro. Por isso ele serve para compartilhar dados entre telas: todas enxergam o mesmo objeto.
export class SimulacaoService { // Declara a classe; o export permite que outros arquivos a importem.
  private readonly http = inject(HttpClient); // Pega o HttpClient e guarda em http. O private significa que só essa classe usa, e o readonly que a variável não pode ser trocada depois.

  readonly dados = signal<SimulacaoRequest | null>(null); // Uma caixinha chamada dados que guarda o que o usuário preencheu, ou null se ainda não há nada. Ela começa em null.
  readonly resultado = signal<SimulacaoResponse | null>(null); // A mesma ideia, mas para a resposta do backend. Também começa em null.

  simular(req: SimulacaoRequest) { // Método que recebe os dados da simulação no parâmetro req
    return this.http // Faz um POST para /api/simulacoes com req no corpo. O <SimulacaoResponse> informa que a resposta virá nesse formato.
      .post<SimulacaoResponse>('/api/simulacoes', req)
      .pipe(tap((res) => this.resultado.set(res))); //Quando a resposta chega, ela é gravada no signal resultado, e a resposta segue adiante para quem chamou o método.
  }
}
