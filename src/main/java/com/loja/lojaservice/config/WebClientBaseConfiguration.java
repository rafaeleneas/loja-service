package com.loja.lojaservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.Duration.ofSeconds;
import static java.util.UUID.randomUUID;

@Slf4j
@RequiredArgsConstructor
public class WebClientBaseConfiguration {
    private final Map<String, ReactiveOAuth2AuthorizedClientManager> authorizedClientManagers = new ConcurrentHashMap<>();

    protected <T> T buildWebClient(String baseUrl, WebClientInfo webclientInfo, Class<T> clientClass) {
        ReactiveOAuth2AuthorizedClientManager authorizedClientManager = null;
        if (webclientInfo != null && StringUtils.hasText(webclientInfo.getId()) && authorizedClientManagers.containsKey(webclientInfo.getId())) {
            authorizedClientManager = authorizedClientManagers.get(webclientInfo.getId());
        } else if (webclientInfo != null && StringUtils.hasText(webclientInfo.getId())) {
            try {
                authorizedClientManager = buildAuthorizedClientManager(webclientInfo);
            } catch (Exception e) {
                log.warn("OAuth2 do WebClient indisponivel para o registro {}, seguindo sem token", webclientInfo.getId(), e);
            }
        }
        String registrationId = webclientInfo != null ? webclientInfo.getId() : null;
        return HttpServiceProxyFactory
                .builderFor(createWebClientAdapter(baseUrl, authorizedClientManager, registrationId))
                .build()
                .createClient(clientClass);
    }

    private ReactiveOAuth2AuthorizedClientManager buildAuthorizedClientManager(WebClientInfo webclientInfo) {
        if (!StringUtils.hasText(webclientInfo.getTokenUri()) && !StringUtils.hasText(webclientInfo.getProvider())) {
            throw new IllegalArgumentException("tokenUri nao informado para o WebClient " + webclientInfo.getId());
        }

        ClientRegistration.Builder registrationBuilder = ClientRegistration
                .withRegistrationId(webclientInfo.getId())
                .clientId(webclientInfo.getClientId())
                .clientSecret(webclientInfo.getClientSecret())
                .authorizationGrantType(resolveGrantType(webclientInfo.getAuthorizationGrantType()))
                .tokenUri(resolveTokenUri(webclientInfo));

        if (StringUtils.hasText(webclientInfo.getIssuerUri())) {
            registrationBuilder.issuerUri(webclientInfo.getIssuerUri());
        }
        if (StringUtils.hasText(webclientInfo.getUserNameAttribute())) {
            registrationBuilder.userNameAttributeName(webclientInfo.getUserNameAttribute());
        }
        if (StringUtils.hasText(webclientInfo.getScope())) {
            registrationBuilder.scope(webclientInfo.getScope());
        }

        ClientRegistration clientRegistration = registrationBuilder.build();

        final InMemoryReactiveClientRegistrationRepository clientRegistrationRepository = new InMemoryReactiveClientRegistrationRepository(clientRegistration);
        final InMemoryReactiveOAuth2AuthorizedClientService authorizedClientService = new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository);

        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);

        ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider = ReactiveOAuth2AuthorizedClientProviderBuilder
                .builder()
                .clientCredentials()
                .build();
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        authorizedClientManagers.put(webclientInfo.getId(), authorizedClientManager);
        return authorizedClientManager;
    }

    private AuthorizationGrantType resolveGrantType(String grantType) {
        if (!StringUtils.hasText(grantType) || AuthorizationGrantType.CLIENT_CREDENTIALS.getValue().equals(grantType)) {
            return AuthorizationGrantType.CLIENT_CREDENTIALS;
        }
        return new AuthorizationGrantType(grantType);
    }

    private String resolveTokenUri(WebClientInfo webclientInfo) {
        if (StringUtils.hasText(webclientInfo.getTokenUri())) {
            return webclientInfo.getTokenUri();
        }
        return webclientInfo.getProvider();
    }

    private ExchangeFilterFunction buildClientFilter(ReactiveOAuth2AuthorizedClientManager authorizedClientManager, String registrationId) {
        final ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Client = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
                authorizedClientManager);
        if (StringUtils.hasText(registrationId)) {
            oauth2Client.setDefaultClientRegistrationId(registrationId);
        }
        return oauth2Client;
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            return Mono.just(clientRequest);
        });
    }

    private ConnectionProvider connectionProvider() {
        return ConnectionProvider.builder(randomUUID().toString())
                .maxConnections(500)
                .maxIdleTime(ofSeconds(20))
                .maxLifeTime(ofSeconds(60))
                .pendingAcquireTimeout(ofSeconds(60))
                .evictInBackground(ofSeconds(120))
                .build();
    }

    private WebClientAdapter createWebClientAdapter(String baseUrl, ReactiveOAuth2AuthorizedClientManager authorizedClientManager, String registrationId) {
        HttpClient httpClient = HttpClient.create(connectionProvider());

        WebClient.Builder webClientBuilder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(logRequest())
                .baseUrl(baseUrl);

        if (authorizedClientManager != null) {
            webClientBuilder.filter(buildClientFilter(authorizedClientManager, registrationId));
        }

        return WebClientAdapter.create(webClientBuilder.build());
    }


}

