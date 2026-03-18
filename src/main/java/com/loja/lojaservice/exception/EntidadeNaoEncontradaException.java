package com.loja.lojaservice.exception;

import java.io.Serial;

public class EntidadeNaoEncontradaException extends LojaException {
    @Serial
    private static final long serialVersionUID = 1L;

    public EntidadeNaoEncontradaException(String message) {
        super(message);
    }
    
}

