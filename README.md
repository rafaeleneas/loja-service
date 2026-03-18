# loja-service

API backend do dominio de loja.

## Tecnologias

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security OAuth2 Resource Server
- Spring Security OAuth2 Client
- Spring Kafka
- PostgreSQL
- Keycloak
- Docker
- Kubernetes

## O que este servico faz

- expõe a API da loja
- gerencia produtos e pedidos
- integra com autenticacao via Keycloak
- publica e consome eventos com Kafka
- conversa com servicos externos, como pagamento-service

## Requisitos

- Java 21
- Maven 3.9+

## Executar localmente

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Configuracoes importantes

- `PAGAMENTO_SERVICE_URL`: URL base do pagamento-service
- `KEYCLOAK_URL` ou `KEYCLOAK_URI`: URL do Keycloak
- `LOJA_SERVICE_INTRACLIENT_SECRET`: segredo OAuth2 para client credentials

## Endpoints uteis

- Swagger UI: `/api/loja/swagger-ui`
- Health: `/api/loja/actuator/health`
- Info: `/api/loja/actuator/info`
