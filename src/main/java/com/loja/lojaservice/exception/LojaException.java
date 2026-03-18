package com.loja.lojaservice.exception;

import java.io.Serial;

public class LojaException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = 1L;

  public LojaException(String mensagem) {
    super(mensagem);
  }
}

