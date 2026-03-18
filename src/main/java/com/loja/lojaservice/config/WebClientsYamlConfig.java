package com.loja.lojaservice.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "web-clients")
@Getter
@Setter
@NoArgsConstructor
public class WebClientsYamlConfig {
    
    private Map<String,WebClientInfo> clients;

    public WebClientInfo getWebClientInfo(String key){
        return clients.get(key);
    }

}

