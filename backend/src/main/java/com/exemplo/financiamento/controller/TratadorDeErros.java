package com.exemplo.financiamento.controller;

import java.util.Map;
import java.util.stream.Collectors;

import com.exemplo.financiamento.service.RegraNegocioException;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TratadorDeErros {

	@ExceptionHandler(RegraNegocioException.class)
	public ResponseEntity<Map<String, String>> regra(RegraNegocioException e) {
		return ResponseEntity.badRequest().body(Map.of("mensagem", e.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> validacao(MethodArgumentNotValidException e) {
		String mensagem = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(campo -> campo.getField() + ": " + campo.getDefaultMessage())
			.collect(Collectors.joining("; "));
		return ResponseEntity.badRequest().body(Map.of("mensagem", mensagem));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, String>> corpoInvalido(HttpMessageNotReadableException e) {
		return ResponseEntity.badRequest().body(Map.of("mensagem", "Dados da simulação inválidos"));
	}

}
