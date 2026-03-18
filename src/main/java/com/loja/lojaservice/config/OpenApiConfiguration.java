package com.loja.lojaservice.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfiguration {

    @Value("${keycloak.url}")
    private String keycloakUrl;

    @Value("${keycloak.realmExtranet}")
    private String realm;

    @Value("${springdoc.url}")
    private String serverUrl;

    @Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(
                new io.swagger.v3.oas.models.info.Info()
                    .title("Loja Service API")
                    .description("ServiÃ§o responsÃ¡vel pelo Loja Service, que gerencia as lojas e seus produtos.")
                    .version("1.0.0")
            )
            .servers(
                List.of(new Server().url(serverUrl))
            )
            .components(new Components()
                .addSecuritySchemes("oauth2", new SecurityScheme()
                    .type(SecurityScheme.Type.OAUTH2)
                    .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                            .authorizationUrl(String.format("%s/realms/%s/protocol/openid-connect/auth", keycloakUrl, realm))
                            .tokenUrl(String.format("%s/realms/%s/protocol/openid-connect/token", keycloakUrl, realm))
                        )
                    )
                )
            )
            .addSecurityItem(new SecurityRequirement().addList("oauth2")
            );
    }
}

