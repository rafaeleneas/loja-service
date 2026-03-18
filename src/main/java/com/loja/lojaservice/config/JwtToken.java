package com.loja.lojaservice.config;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JwtToken {
    private String identificacao;
    private List<String> cargos;
    private String id;
    private String valor;
    private String algoritmo;
}

