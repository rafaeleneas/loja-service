package com.loja.lojaservice.exception;

import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class GlobalExceptionHandlerController {
    @ExceptionHandler(LojaException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErroDetalhe> handleLojaException(LojaException ex, ServletWebRequest request) {
        List<String> erros = List.of(ex.getMessage());
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        log.error(obtemMensagemComPathDaChamada(ex, request));
        ErroDetalhe erroAPI = new ErroDetalhe(
                "Erro de validaÃ§Ã£o",
                request,
                httpStatus,
                erros);
        return ResponseEntity
                .status(httpStatus)
                .body(erroAPI);
    }

    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErroDetalhe> handleEntidadeNaoEncontradaException(EntidadeNaoEncontradaException ex, ServletWebRequest request) {
        List<String> erros = List.of(ex.getMessage());
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        log.error(obtemMensagemComPathDaChamada(ex, request));
        ErroDetalhe erroAPI = new ErroDetalhe(
                "Entidade nÃ£o encontrada",
                request,
                httpStatus,
                erros);
        return ResponseEntity
                .status(httpStatus)
                .body(erroAPI);
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ErroDetalhe> handleAccessDeniedException(AccessDeniedException ex,
            ServletWebRequest request) {
        HttpStatus httpStatus = HttpStatus.FORBIDDEN;
        log.error(obtemMensagemComPathDaChamada(ex, request));
        ErroDetalhe erroAPI = new ErroDetalhe(ex, request, httpStatus);
        return ResponseEntity
                .status(httpStatus)
                .body(erroAPI);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<ErroDetalhe> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, ServletWebRequest request) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        log.error(obtemMensagemComPathDaChamada(ex, request));
        ErroDetalhe erroAPI = new ErroDetalhe(
                "Violacao de integridade de dados",
                request,
                httpStatus,
                List.of("Verifique campos obrigatorios, unicos e relacionamentos informados.")
        );
        return ResponseEntity
                .status(httpStatus)
                .body(erroAPI);
    }

    @ExceptionHandler(Exception.class)
	protected ResponseEntity<ErroDetalhe> handleException(Exception ex, ServletWebRequest request) {
		log.error(obtemMensagemComPathDaChamada(ex, request), ex);
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        ErroDetalhe erroAPI = new ErroDetalhe(ex, request, httpStatus);
		return ResponseEntity.status(httpStatus).body(erroAPI);
	}

    public String obtemMensagemComPathDaChamada(Exception e, ServletWebRequest request) {
		return String.format("%s: %s", request.getRequest().getRequestURI(), e.getMessage());
	}

}

