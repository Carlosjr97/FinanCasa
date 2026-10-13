// Em produção o Spring Boot também entrega o site do Angular. Endereços como /simular ou /resultado não existem como arquivo no servidor: são rotas do Angular. Aqui devolvemos o index.html para elas, e o Angular decide qual tela mostrar.
package com.exemplo.financiamento.controller; // Declara o pacote (pasta lógica) da classe. Tem que bater com o caminho da pasta.

import org.springframework.stereotype.Controller; // Importa a anotação de controller que devolve páginas (e não JSON).
import org.springframework.web.bind.annotation.GetMapping; // Importa a anotação que liga um método ao verbo GET.

@Controller // Diferente do @RestController: o retorno do método é o nome de uma página, não um JSON.
public class SpaController { // Início da classe.

	@GetMapping("/{rota:[^\\.]*}") // Atende qualquer endereço de um nível só e sem ponto (/simular, /modelo, /resultado). Arquivos como main.js têm ponto e continuam sendo entregues normalmente; /api/simulacoes tem dois níveis e não cai aqui.
	public String index() {
		return "forward:/index.html"; // Entrega o index.html do Angular sem mudar o endereço que aparece no navegador.
	}

}
