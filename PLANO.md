# Simulador de Financiamento Imobiliário — Plano de Projeto

7 de outubro de 2026

## Visão geral

O projeto é um simulador de financiamento imobiliário: a pessoa informa nome, idade e renda, o sistema define prazo, faixa, taxa de juros e subsídio, e mostra as parcelas em Price e em SAC. O backend (Java + Spring Boot) guarda todas as regras e cálculos; o frontend (Angular) só coleta dados e exibe o resultado.

Fluxo de telas:

1. **Identificação** — nome, idade, tipo de renda (própria ou familiar), valor da renda, valor do imóvel e entrada.
2. **Enquadramento** — mostra a faixa, o prazo máximo, a taxa sorteada, o subsídio sorteado (faixas 1 e 2) e o valor a financiar.
3. **Escolha do sistema** — a pessoa alterna entre Price e SAC e vê a tabela de parcelas de cada um.

Três decisões de desenho que simplificam tudo:

- **Uma única chamada de API** devolve o enquadramento e as duas tabelas (Price e SAC). Assim a taxa e o subsídio são sorteados uma vez só e não mudam quando a pessoa troca de sistema.
- **Sem banco de dados na primeira versão.** A simulação não precisa ser salva; persistência fica como etapa opcional.
- **O "login" é um formulário de identificação**, sem senha. Autenticação de verdade (Spring Security) só se você quiser guardar simulações por usuário.

## Regras de negócio

São três tabelas de regra — prazo por idade, faixa por renda e subsídio — mais cinco pontos que a sua descrição deixou em aberto.

### Prazo máximo por idade

| Idade | Prazo | Em meses |
| --- | --- | --- |
| 18 a 49 anos | 35 anos | 420 |
| 50 a 54 anos | 27 anos e 6 meses | 330 |
| 55 a 59 anos | 25 anos | 300 |
| 60 a 64 anos | 20 anos | 240 |
| 65 a 69 anos | 15 anos | 180 |
| 70 anos ou mais | 10 anos | 120 |

### Faixa e taxa de juros por renda

| Faixa | Renda mensal | Taxa ao ano | Subsídio |
| --- | --- | --- | --- |
| 1 | até R$ 3.200,00 | sorteada entre 4,00% e 5,00% | sim |
| 2 | de R$ 3.200,01 a R$ 5.000,00 | sorteada entre 4,75% e 7,00% | sim |
| 3 | de R$ 5.000,01 a R$ 9.600,00 | sorteada entre 7,66% e 8,16% | não |
| 4 | de R$ 9.600,01 a R$ 13.000,00 | 10,00% fixa | não |

### Subsídio

Nas faixas 1 e 2 o sistema sorteia um subsídio de até R$ 65.000,00, que abate do valor do imóvel. Valor financiado = valor do imóvel − entrada − subsídio.

### Pontos em aberto

O plano já adota uma suposição para cada um; troque se a regra real for outra.

| Ponto | O que faltou | Suposição adotada no plano |
| --- | --- | --- |
| Idade de 54 anos | A descrição pula de "50 a 53" para "55 a 59" | 54 entra no grupo de 27 anos e 6 meses |
| Valor do imóvel e entrada | O subsídio abate do valor da casa, mas esse valor não é pedido na tela | Dois campos a mais no formulário |
| Limites das faixas | R$ 3.200,00 exatos aparece em duas faixas | O valor exato do limite fica na faixa de baixo |
| Renda acima de R$ 13.000,00 | Sem regra | Mensagem "renda fora das faixas do programa" |
| Valor mínimo do subsídio | Só o teto foi dado | Sorteio entre R$ 0,00 e R$ 65.000,00, limitado ao valor a financiar |

Menores de 18 anos recebem erro de validação. Não há teto de idade: qualquer pessoa com 70 anos ou mais financia em 10 anos.

## Cálculos

As parcelas são mensais, então o primeiro passo é converter a taxa anual sorteada em taxa mensal equivalente. Depois, Price dá parcela fixa e SAC dá amortização fixa com parcela decrescente.

Nas fórmulas: PV = valor financiado, n = prazo em meses, i = taxa mensal, ia = taxa anual.

**Taxa mensal equivalente**

```text
i = (1 + ia)^(1/12) − 1
```

**Price — parcela fixa**

```text
PMT = PV × i / (1 − (1 + i)^(−n))
```

Em cada mês: juros = saldo × i; amortização = PMT − juros; novo saldo = saldo − amortização.

**SAC — amortização fixa**

```text
A = PV / n
juros do mês k   = saldo anterior × i
parcela do mês k = A + juros do mês k
```

Em cada mês: juros = saldo × i; parcela = A + juros; novo saldo = saldo − A.

### Exemplo para conferir o código

Imóvel de R$ 200.000,00, entrada de R$ 40.000,00, subsídio de R$ 30.000,00, taxa de 5,00% ao ano, 30 anos de idade (420 meses). Valor financiado: R$ 130.000,00. Taxa mensal: 0,4074%.

| | Price | SAC |
| --- | --- | --- |
| Primeira parcela | R$ 646,92 | R$ 839,16 |
| Última parcela | R$ 646,92 | R$ 310,78 |
| Total de juros | cerca de R$ 141.700 | cerca de R$ 111.500 |

Os valores foram calculados à mão e podem variar em centavos conforme o arredondamento. Use-os como teste automatizado no backend.

Duas escolhas a manter fixas: taxa mensal por juros compostos (fórmula acima), não a divisão simples por 12; e arredondamento em 2 casas só na hora de exibir, com a última parcela absorvendo a diferença de centavos para o saldo zerar.

## Arquitetura

São dois projetos na mesma pasta: `backend` (API REST em Spring Boot, porta 8080) e `frontend` (Angular, porta 4200). O Angular chama um único endpoint, `POST /api/simulacoes`.

```text
financiamento-imovel/
├── backend/
│   └── src/main/java/com/exemplo/financiamento/
│       ├── controller/   SimulacaoController, TratadorDeErros
│       ├── dto/          SimulacaoRequest, SimulacaoResponse, Parcela, TabelaAmortizacao
│       ├── model/        Faixa (enum), TipoRenda (enum)
│       └── service/      RegrasService, CalculoService, SimulacaoService
└── frontend/
    └── src/app/
        ├── models/       simulacao.ts
        ├── services/     simulacao.service.ts
        └── pages/        identificacao/, resultado/
```

### Responsabilidade de cada classe do backend

| Classe | O que faz |
| --- | --- |
| `Faixa` | Guarda renda máxima, taxa mínima, taxa máxima e se tem subsídio |
| `RegrasService` | Prazo por idade, faixa por renda, sorteio da taxa e do subsídio |
| `CalculoService` | Taxa mensal, tabela Price, tabela SAC |
| `SimulacaoService` | Junta as duas anteriores e monta a resposta |
| `SimulacaoController` | Recebe o POST, valida e devolve o JSON |
| `TratadorDeErros` | Converte erros de validação e de regra em resposta 400 com mensagem |

### Contrato da API

Requisição `POST /api/simulacoes`:

```json
{
  "nome": "Maria Silva",
  "idade": 30,
  "tipoRenda": "FAMILIAR",
  "renda": 3000.00,
  "valorImovel": 200000.00,
  "entrada": 40000.00
}
```

Resposta `200 OK` (a lista de parcelas tem um item por mês; aqui só o primeiro):

```json
{
  "nome": "Maria Silva",
  "faixa": "FAIXA_1",
  "prazoMeses": 420,
  "taxaAnual": 4.62,
  "taxaMensal": 0.3771,
  "subsidio": 30000.00,
  "valorFinanciado": 130000.00,
  "price": {
    "primeiraParcela": 619.33,
    "ultimaParcela": 619.33,
    "totalJuros": 130118.60,
    "totalPago": 260118.60,
    "parcelas": [
      { "numero": 1, "valor": 619.33, "juros": 490.23, "amortizacao": 129.10, "saldoDevedor": 129870.90 }
    ]
  },
  "sac": { "...": "mesma estrutura" }
}
```

Os números desse JSON são ilustrativos, só para mostrar o formato. Erros de validação ou de regra (menor de 18, renda acima de R$ 13.000,00, entrada maior que o imóvel) devolvem `400` com `{ "mensagem": "..." }`.

## Pré-requisitos

Você precisa de quatro ferramentas instaladas no Windows antes de começar.

| Ferramenta | Para quê | Como conferir |
| --- | --- | --- |
| JDK 21 (LTS) | Rodar o Spring Boot | `java -version` |
| Node.js LTS | Rodar o Angular | `node -v` |
| Angular CLI | Criar e servir o frontend | `npm install -g @angular/cli` e depois `ng version` |
| IDE | Editar o código | IntelliJ IDEA Community para o backend, VS Code para o frontend |

O Maven não precisa ser instalado: o projeto gerado traz o `mvnw.cmd`.

Um aviso sobre a pasta: o projeto está hoje dentro do OneDrive (Área de Trabalho). O `node_modules` do Angular tem dezenas de milhares de arquivos e a sincronização costuma travar ou corromper a instalação. Crie o código em uma pasta fora do OneDrive, por exemplo `C:\dev\financiamento-imovel`.

## Passo a passo do backend

O backend fica pronto em seis passos, na ordem: gerar o projeto, DTOs, enum de faixas, regras, cálculos e controller. Os trechos abaixo são o núcleo de cada classe; imports e getters ficam por sua conta.

### 1. Gerar o projeto

Em [start.spring.io](https://start.spring.io): Maven, Java 21, versão estável mais recente do Spring Boot, group `com.exemplo`, artifact `financiamento`. Dependências: **Spring Web** e **Validation**. Descompacte em `backend/` e rode `.\mvnw.cmd spring-boot:run` para confirmar que sobe na porta 8080.

### 2. DTOs de entrada e saída

```java
public enum TipoRenda { PROPRIA, FAMILIAR }

public record SimulacaoRequest(
    @NotBlank String nome,
    @NotNull @Min(18) Integer idade,
    @NotNull TipoRenda tipoRenda,
    @NotNull @Positive BigDecimal renda,
    @NotNull @Positive BigDecimal valorImovel,
    @NotNull @PositiveOrZero BigDecimal entrada) {}

public record Parcela(int numero, BigDecimal valor, BigDecimal juros,
                      BigDecimal amortizacao, BigDecimal saldoDevedor) {}

public record TabelaAmortizacao(BigDecimal primeiraParcela, BigDecimal ultimaParcela,
                                BigDecimal totalJuros, BigDecimal totalPago,
                                List<Parcela> parcelas) {}

public record SimulacaoResponse(String nome, Faixa faixa, int prazoMeses,
                                BigDecimal taxaAnual, BigDecimal taxaMensal,
                                BigDecimal subsidio, BigDecimal valorFinanciado,
                                TabelaAmortizacao price, TabelaAmortizacao sac) {}
```

### 3. Enum das faixas

As quatro faixas viram dados, não `if`. Mudar um limite ou uma taxa passa a ser trocar um número.

```java
public enum Faixa {
  FAIXA_1("3200.00", 4.00, 5.00, true),
  FAIXA_2("5000.00", 4.75, 7.00, true),
  FAIXA_3("9600.00", 7.66, 8.16, false),
  FAIXA_4("13000.00", 10.00, 10.00, false);

  private final BigDecimal rendaMaxima;
  private final double taxaMinima;
  private final double taxaMaxima;
  private final boolean temSubsidio;

  Faixa(String rendaMaxima, double taxaMinima, double taxaMaxima, boolean temSubsidio) {
    this.rendaMaxima = new BigDecimal(rendaMaxima);
    this.taxaMinima = taxaMinima;
    this.taxaMaxima = taxaMaxima;
    this.temSubsidio = temSubsidio;
  }

  public static Faixa de(BigDecimal renda) {
    return Arrays.stream(values())
        .filter(f -> renda.compareTo(f.rendaMaxima) <= 0)
        .findFirst()
        .orElseThrow(() -> new RegraNegocioException(
            "Renda acima de R$ 13.000,00 está fora das faixas do programa"));
  }
  // getters
}
```

`RegraNegocioException` é uma `RuntimeException` simples criada por você.

### 4. RegrasService

O gerador aleatório entra pelo construtor para que os testes possam fixar o sorteio.

```java
@Service
public class RegrasService {
  private static final double SUBSIDIO_MAXIMO = 65_000.00;
  private final Random random;

  public RegrasService() { this(new Random()); }
  RegrasService(Random random) { this.random = random; }

  public int prazoMeses(int idade) {
    if (idade < 18) throw new RegraNegocioException("Idade mínima é 18 anos");
    if (idade <= 49) return 420;
    if (idade <= 54) return 330;
    if (idade <= 59) return 300;
    if (idade <= 64) return 240;
    if (idade <= 69) return 180;
    return 120;
  }

  public BigDecimal sortearTaxaAnual(Faixa faixa) {
    double taxa = faixa.getTaxaMinima()
        + random.nextDouble() * (faixa.getTaxaMaxima() - faixa.getTaxaMinima());
    return BigDecimal.valueOf(taxa).setScale(2, RoundingMode.HALF_UP);
  }

  public BigDecimal sortearSubsidio(Faixa faixa, BigDecimal saldoAposEntrada) {
    if (!faixa.isTemSubsidio()) return BigDecimal.ZERO.setScale(2);
    BigDecimal sorteado = BigDecimal.valueOf(random.nextDouble() * SUBSIDIO_MAXIMO)
        .setScale(2, RoundingMode.HALF_UP);
    return sorteado.min(saldoAposEntrada);
  }
}
```

### 5. CalculoService

As contas internas usam `double`; o arredondamento para 2 casas acontece só ao montar cada `Parcela`.

```java
@Service
public class CalculoService {

  public double taxaMensal(BigDecimal taxaAnualPercentual) {
    return Math.pow(1 + taxaAnualPercentual.doubleValue() / 100, 1.0 / 12) - 1;
  }

  public TabelaAmortizacao price(BigDecimal valorFinanciado, double i, int n) {
    double saldo = valorFinanciado.doubleValue();
    double pmt = saldo * i / (1 - Math.pow(1 + i, -n));
    List<Parcela> parcelas = new ArrayList<>();
    for (int k = 1; k <= n; k++) {
      double juros = saldo * i;
      double amortizacao = (k == n) ? saldo : pmt - juros;
      saldo -= amortizacao;
      parcelas.add(parcela(k, amortizacao + juros, juros, amortizacao, saldo));
    }
    return resumo(parcelas);
  }

  public TabelaAmortizacao sac(BigDecimal valorFinanciado, double i, int n) {
    double saldo = valorFinanciado.doubleValue();
    double amortizacaoFixa = saldo / n;
    List<Parcela> parcelas = new ArrayList<>();
    for (int k = 1; k <= n; k++) {
      double juros = saldo * i;
      double amortizacao = (k == n) ? saldo : amortizacaoFixa;
      saldo -= amortizacao;
      parcelas.add(parcela(k, amortizacao + juros, juros, amortizacao, saldo));
    }
    return resumo(parcelas);
  }

  private Parcela parcela(int k, double valor, double juros, double amort, double saldo) {
    return new Parcela(k, arredondar(valor), arredondar(juros),
        arredondar(amort), arredondar(Math.max(saldo, 0)));
  }

  private TabelaAmortizacao resumo(List<Parcela> p) {
    BigDecimal totalJuros = p.stream().map(Parcela::juros).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal totalPago = p.stream().map(Parcela::valor).reduce(BigDecimal.ZERO, BigDecimal::add);
    return new TabelaAmortizacao(p.get(0).valor(), p.get(p.size() - 1).valor(),
        totalJuros, totalPago, p);
  }

  private BigDecimal arredondar(double v) {
    return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
  }
}
```

### 6. SimulacaoService, controller e tratamento de erros

```java
@Service
public class SimulacaoService {
  private final RegrasService regras;
  private final CalculoService calculo;
  // construtor com os dois

  public SimulacaoResponse simular(SimulacaoRequest req) {
    BigDecimal saldoAposEntrada = req.valorImovel().subtract(req.entrada());
    if (saldoAposEntrada.signum() <= 0)
      throw new RegraNegocioException("A entrada deve ser menor que o valor do imóvel");

    int prazo = regras.prazoMeses(req.idade());
    Faixa faixa = Faixa.de(req.renda());
    BigDecimal taxaAnual = regras.sortearTaxaAnual(faixa);
    BigDecimal subsidio = regras.sortearSubsidio(faixa, saldoAposEntrada);
    BigDecimal financiado = saldoAposEntrada.subtract(subsidio);
    double i = calculo.taxaMensal(taxaAnual);

    return new SimulacaoResponse(req.nome(), faixa, prazo, taxaAnual,
        BigDecimal.valueOf(i * 100).setScale(4, RoundingMode.HALF_UP),
        subsidio, financiado,
        calculo.price(financiado, i, prazo), calculo.sac(financiado, i, prazo));
  }
}

@RestController
@RequestMapping("/api/simulacoes")
public class SimulacaoController {
  private final SimulacaoService service;
  // construtor

  @PostMapping
  public SimulacaoResponse simular(@Valid @RequestBody SimulacaoRequest req) {
    return service.simular(req);
  }
}

@RestControllerAdvice
public class TratadorDeErros {
  @ExceptionHandler(RegraNegocioException.class)
  public ResponseEntity<Map<String, String>> regra(RegraNegocioException e) {
    return ResponseEntity.badRequest().body(Map.of("mensagem", e.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> validacao(MethodArgumentNotValidException e) {
    String msg = e.getBindingResult().getFieldErrors().stream()
        .map(f -> f.getField() + ": " + f.getDefaultMessage())
        .collect(Collectors.joining("; "));
    return ResponseEntity.badRequest().body(Map.of("mensagem", msg));
  }
}
```

Se o subsídio sorteado cobrir todo o saldo, o valor financiado fica zero e as tabelas saem com todas as linhas zeradas. Trate esse caso no `SimulacaoService` devolvendo tabelas vazias ou uma mensagem "imóvel quitado pelo subsídio".

Teste com o backend rodando:

```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/simulacoes -ContentType "application/json" -Body '{"nome":"Maria","idade":30,"tipoRenda":"FAMILIAR","renda":3000,"valorImovel":200000,"entrada":40000}'
```

## Passo a passo do frontend

O frontend tem duas páginas (identificação e resultado), um serviço HTTP e um proxy para o backend. Os trechos usam o Angular atual: componentes standalone, `inject()`, signals e a sintaxe `@if` / `@for`.

### 1. Criar o projeto

```powershell
ng new frontend --routing --style=scss
cd frontend
ng generate component pages/identificacao
ng generate component pages/resultado
ng generate service services/simulacao
```

### 2. Proxy para o backend

O proxy evita configurar CORS no Spring durante o desenvolvimento. Crie `frontend/proxy.conf.json`:

```json
{
  "/api": { "target": "http://localhost:8080", "secure": false }
}
```

Suba o Angular sempre com `ng serve --proxy-config proxy.conf.json` (ou registre o arquivo em `angular.json`, em `serve.options.proxyConfig`).

### 3. Modelos

`src/app/models/simulacao.ts` espelha o contrato da API:

```typescript
export type TipoRenda = 'PROPRIA' | 'FAMILIAR';
export type Sistema = 'price' | 'sac';

export interface SimulacaoRequest {
  nome: string;
  idade: number;
  tipoRenda: TipoRenda;
  renda: number;
  valorImovel: number;
  entrada: number;
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
  faixa: 'FAIXA_1' | 'FAIXA_2' | 'FAIXA_3' | 'FAIXA_4';
  prazoMeses: number;
  taxaAnual: number;
  taxaMensal: number;
  subsidio: number;
  valorFinanciado: number;
  price: TabelaAmortizacao;
  sac: TabelaAmortizacao;
}
```

### 4. Serviço

O serviço faz a chamada e guarda o resultado em um signal, para a página de resultado ler depois da navegação. Adicione `provideHttpClient()` aos providers em `app.config.ts`.

```typescript
@Injectable({ providedIn: 'root' })
export class SimulacaoService {
  private http = inject(HttpClient);
  readonly resultado = signal<SimulacaoResponse | null>(null);

  simular(req: SimulacaoRequest) {
    return this.http
      .post<SimulacaoResponse>('/api/simulacoes', req)
      .pipe(tap(res => this.resultado.set(res)));
  }
}
```

### 5. Rotas

```typescript
export const routes: Routes = [
  { path: '', component: IdentificacaoComponent },
  { path: 'resultado', component: ResultadoComponent },
  { path: '**', redirectTo: '' },
];
```

### 6. Página de identificação

Formulário reativo com as mesmas validações do backend. O backend continua sendo a autoridade; a validação aqui só dá resposta rápida.

```typescript
@Component({
  selector: 'app-identificacao',
  imports: [ReactiveFormsModule],
  templateUrl: './identificacao.component.html',
})
export class IdentificacaoComponent {
  private fb = inject(FormBuilder);
  private service = inject(SimulacaoService);
  private router = inject(Router);

  erro = signal<string | null>(null);
  carregando = signal(false);

  form = this.fb.nonNullable.group({
    nome: ['', Validators.required],
    idade: [null as number | null, [Validators.required, Validators.min(18)]],
    tipoRenda: ['PROPRIA' as TipoRenda, Validators.required],
    renda: [null as number | null, [Validators.required, Validators.min(0.01)]],
    valorImovel: [null as number | null, [Validators.required, Validators.min(0.01)]],
    entrada: [0, [Validators.required, Validators.min(0)]],
  });

  enviar() {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.carregando.set(true);
    this.erro.set(null);
    this.service.simular(this.form.getRawValue() as SimulacaoRequest).subscribe({
      next: () => this.router.navigate(['/resultado']),
      error: e => {
        this.erro.set(e.error?.mensagem ?? 'Não foi possível simular. Tente de novo.');
        this.carregando.set(false);
      },
    });
  }
}
```

No HTML: um `<form [formGroup]="form" (ngSubmit)="enviar()">` com um campo por controle, dois botões de rádio para `tipoRenda` ("Renda própria" e "Renda familiar") e a mensagem de erro em `@if (erro()) { ... }`.

### 7. Página de resultado

```typescript
@Component({
  selector: 'app-resultado',
  imports: [CurrencyPipe, DecimalPipe],
  templateUrl: './resultado.component.html',
})
export class ResultadoComponent {
  private service = inject(SimulacaoService);
  private router = inject(Router);

  resultado = this.service.resultado;
  sistema = signal<Sistema>('price');
  tabela = computed(() => this.resultado()?.[this.sistema()] ?? null);

  constructor() {
    if (!this.resultado()) this.router.navigate(['/']);
  }
}
```

```html
@if (resultado(); as r) {
  <h1>Olá, {{ r.nome }}</h1>
  <p>Prazo: {{ r.prazoMeses }} meses · Taxa: {{ r.taxaAnual | number:'1.2-2' }}% ao ano</p>
  <p>Subsídio: {{ r.subsidio | currency:'BRL' }} · Financiado: {{ r.valorFinanciado | currency:'BRL' }}</p>

  <button (click)="sistema.set('price')" [class.ativo]="sistema() === 'price'">Price</button>
  <button (click)="sistema.set('sac')" [class.ativo]="sistema() === 'sac'">SAC</button>

  @if (tabela(); as t) {
    <p>Primeira parcela: {{ t.primeiraParcela | currency:'BRL' }} · Última: {{ t.ultimaParcela | currency:'BRL' }}</p>
    <p>Total de juros: {{ t.totalJuros | currency:'BRL' }}</p>
    <table>
      <thead><tr><th>Nº</th><th>Parcela</th><th>Juros</th><th>Amortização</th><th>Saldo</th></tr></thead>
      <tbody>
        @for (p of t.parcelas; track p.numero) {
          <tr>
            <td>{{ p.numero }}</td>
            <td>{{ p.valor | currency:'BRL' }}</td>
            <td>{{ p.juros | currency:'BRL' }}</td>
            <td>{{ p.amortizacao | currency:'BRL' }}</td>
            <td>{{ p.saldoDevedor | currency:'BRL' }}</td>
          </tr>
        }
      </tbody>
    </table>
  }
}
```

Para os valores saírem como `R$ 1.234,56`, registre o locale brasileiro em `app.config.ts`: `registerLocaleData(localePt)` e o provider `{ provide: LOCALE_ID, useValue: 'pt-BR' }`.

A tabela pode ter até 420 linhas. Na primeira versão, mostre tudo com rolagem; paginação por ano é uma melhoria posterior.

## Testes e verificação

As regras ficam todas em `RegrasService`, `Faixa` e `CalculoService`, então testes unitários com JUnit (já vem no projeto) cobrem quase tudo sem subir o servidor. Rode com `.\mvnw.cmd test`.

### Casos de limite

| Entrada | Resultado esperado |
| --- | --- |
| Idade 17 | Erro "Idade mínima é 18 anos" |
| Idade 18 e 49 | 420 meses |
| Idade 50 e 54 | 330 meses |
| Idade 55 e 59 | 300 meses |
| Idade 60 e 64 | 240 meses |
| Idade 65 e 69 | 180 meses |
| Idade 70 e 85 | 120 meses |
| Renda R$ 3.200,00 | Faixa 1 |
| Renda R$ 3.200,01 | Faixa 2 |
| Renda R$ 5.000,01 | Faixa 3 |
| Renda R$ 9.600,01 | Faixa 4 |
| Renda R$ 13.000,01 | Erro "fora das faixas do programa" |
| Entrada igual ou maior que o imóvel | Erro "A entrada deve ser menor que o valor do imóvel" |

### Sorteios

- Repita o sorteio 1.000 vezes por faixa e confirme que a taxa nunca sai do intervalo da faixa.
- Faixa 4 devolve sempre 10,00%.
- Faixas 3 e 4 devolvem subsídio zero; faixas 1 e 2 devolvem entre R$ 0,00 e R$ 65.000,00.
- O subsídio nunca passa de (valor do imóvel − entrada).

### Cálculos

- Exemplo da seção Cálculos: R$ 130.000,00, 5,00% ao ano, 420 meses → Price R$ 646,92; SAC começa em R$ 839,16 e termina em R$ 310,78.
- O saldo devedor da última parcela é R$ 0,00 nos dois sistemas.
- A soma das amortizações é igual ao valor financiado.
- No SAC, cada parcela é menor que a anterior; na Price, todas são iguais (a última pode diferir em centavos).

### Conferência manual de ponta a ponta

- [ ] Backend e frontend sobem sem erro
- [ ] Formulário vazio não envia e mostra os campos inválidos
- [ ] Simulação válida leva à tela de resultado
- [ ] Alternar Price e SAC troca a tabela sem mudar taxa nem subsídio
- [ ] Erro do backend (renda de R$ 20.000,00) aparece como mensagem na tela
- [ ] Abrir `/resultado` direto volta para a identificação

## Ordem de construção

Construa o backend inteiro primeiro e teste pelo terminal; só depois comece o Angular. Assim cada problema aparece em um lugar só.

**Etapa 1 — Ambiente**

- [ ] Instalar JDK 21, Node.js LTS e Angular CLI
- [ ] Criar a pasta do projeto fora do OneDrive
- [ ] Decidir os cinco pontos em aberto das regras

**Etapa 2 — Regras e cálculos**

- [ ] Gerar o projeto no start.spring.io
- [ ] Criar `Faixa`, `RegrasService` e seus testes
- [ ] Criar `CalculoService` e os testes com o exemplo numérico

**Etapa 3 — API**

- [ ] Criar DTOs, `SimulacaoService`, controller e `TratadorDeErros`
- [ ] Testar o endpoint com `Invoke-RestMethod`

**Etapa 4 — Frontend**

- [ ] Criar o projeto Angular, o proxy, os modelos e o serviço
- [ ] Montar a página de identificação
- [ ] Montar a página de resultado com a troca Price / SAC

**Etapa 5 — Acabamento**

- [ ] Estilo das telas e locale pt-BR
- [ ] Passar pela conferência manual de ponta a ponta

### Melhorias para depois

- Aviso quando a primeira parcela passar de 30% da renda, limite usual de comprometimento em financiamento habitacional.
- Gráfico comparando a evolução das parcelas em Price e SAC.
- Salvar simulações em banco (Spring Data JPA com H2 ou PostgreSQL).
- Login de verdade com Spring Security, se as simulações forem guardadas por usuário.
- Exportar a tabela de parcelas em PDF ou CSV.
