// aqui é a porta de entrada HTTP do backend: recebe o POST /api/simulacoes vindo do angular, validamos os dados e repassa para o simulacaoservice, devolvendo o resultado como JSON. Não famos nenhum cálculo, só recebemos e delega.
package com.exemplo.financiamento.controller; // Declara o pacote (pasta lógica) da classe. Tem que bater com o caminho da pasta.

import com.exemplo.financiamento.dto.SimulacaoRequest; // importando DTOs: formato que os dados entram
import com.exemplo.financiamento.dto.SimulacaoResponse; // importando DTOs: formato que os dados saem
import com.exemplo.financiamento.service.SimulacaoService; //Importa o serviço que contém a lógica da simulação.

import jakarta.validation.Valid; // Importa a anotação que dispara a validação dos campos.

import org.springframework.web.bind.annotation.PostMapping; // Importa a anotação que dispara a validação dos campos.
import org.springframework.web.bind.annotation.RequestBody; // Importa a anotação que dispara a validação dos campos.
import org.springframework.web.bind.annotation.RequestMapping; // Importa a anotação que dispara a validação dos campos.
import org.springframework.web.bind.annotation.RestController; // Importa a anotação que dispara a validação dos campos.

@RestController  // Diz ao Spring que esta classe atende requisições HTTP e que o retorno dos métodos vira JSON automaticamente.
@RequestMapping("/api/simulacoes") //  Define o endereço base: tudo nesta classe responde em /api/simulacoes.
public class SimulacaoController { //  Início da classe.

	private final SimulacaoService service; // Guarda a referência ao serviço. final significa que é atribuída uma vez e não muda mais.

	public SimulacaoController(SimulacaoService service) { //  Construtor. O Spring cria o SimulacaoService e o entrega aqui sozinho (injeção de dependência); você nunca escreve new SimulacaoService().
		this.service = service;
	}

	@PostMapping //  O método abaixo responde ao verbo POST no endereço base, ou seja, POST /api/simulacoes.
	public SimulacaoResponse simular(@Valid @RequestBody SimulacaoRequest req) { //  @RequestBody converte o JSON do corpo da requisição em um objeto SimulacaoRequest. @Valid checa as regras de validação declaradas nesse DTO; se falhar, o Spring lança uma exceção e o método nem é executado. O tipo de retorno SimulacaoResponse é o que volta para o frontend.
		return service.simular(req); // Passa os dados para o serviço e devolve o resultado, que o Spring converte em JSON.
	}

}
