package com.loja.lojaservice.config;

import com.loja.lojaservice.client.pagamento.PagamentoApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebClientConfiguration extends WebClientBaseConfiguration {
    private static final String REGISTRATION_LOJA_SERVICE_ID = "loja-service";
    private final WebClientsYamlConfig webclientsYamlConfig;

    public WebClientConfiguration(WebClientsYamlConfig webclientsYamlConfig) {
        this.webclientsYamlConfig = webclientsYamlConfig;
    }

    @Bean
    PagamentoApiClient pagamentoApiClient(@Value("${servicos.pagamento-service}") String pagamentoServiceUrl) {
        WebClientInfo webclientInfo = webclientsYamlConfig.getWebClientInfo(REGISTRATION_LOJA_SERVICE_ID);
        return buildWebClient(pagamentoServiceUrl, webclientInfo, PagamentoApiClient.class);
    }
}

