package com.loja.lojaservice.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.ServletWebRequest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErroDetalhe {

    String message;
    String path;
    int status;
    List<String> errors;
    String timeStamp;

    private void populeFields(String newMessage, ServletWebRequest request, HttpStatus status) {
        message = newMessage;
        path = request.getRequest().getRequestURI();
        this.status = status.value();
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        timeStamp = LocalDateTime.now().format(dateTimeFormat);
    }

    public ErroDetalhe(Exception ex, ServletWebRequest request, HttpStatus status) {
        populeFields(ex.getMessage(), request, status);
        errors = new ArrayList<>();
        errors.add(status.name());
    }

    public ErroDetalhe(String message, ServletWebRequest request, HttpStatus status, List<String> newErrors) {
        populeFields(message, request, status);
        errors = new ArrayList<>(newErrors);
    }
}
