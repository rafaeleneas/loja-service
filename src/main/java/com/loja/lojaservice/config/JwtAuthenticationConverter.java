package com.loja.lojaservice.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import lombok.Getter;

/**
 * Extraia o JWT para adicionar o prefixo ROLE_ a cada funÃ§Ã£o para que o
 * Spring possa interpretÃ¡-los.
 */
@Component
@Getter
@RequiredArgsConstructor
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String PREFERRED_USERNAME = "preferred_username";
    private static final String REALM_ACCESS = "realm_access";
    private static final String SUB = "sub";
    private static final String ROLES = "roles";
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> jwtAuthorities = jwtGrantedAuthoritiesConverter.convert(jwt);
        if (jwtAuthorities == null) {
            throw new IllegalArgumentException("jwtGrantedAuthoritiesConverter.convert(jwt) returned null");
        }
        Collection<? extends GrantedAuthority> realmAuthorities = extractAuthorities(jwt);
        Collection<GrantedAuthority> authorities = new ArrayList<>(jwtAuthorities);
        authorities.addAll(realmAuthorities);
        return new JwtAuthenticationToken(jwt, authorities, getPrincipleClaimName(jwt));
    }

    public Jwt convertJwt(JwtToken jwtToken)  {
        Map<String, List<String>> realmAccess = Map.of(ROLES, jwtToken.getCargos());
        return Jwt.withTokenValue(jwtToken.getValor())
                .header("alg", jwtToken.getAlgoritmo())
                .claim(REALM_ACCESS, realmAccess)
                .claim(PREFERRED_USERNAME, jwtToken.getIdentificacao())
                .build();
    }

    /**
     * Extrai as funÃ§Ãµes do token JWT e as formata para serem interpretadas pelo
     * Spring Security.
     * Por exemplo: Se 'admin_role' for recebido, 'ROLE_admin_role' ser retornado.
     *
     * @param jwt O token de autenticaÃ§Ã£o JWT enviado na solicitaÃ§Ã£o.
     * @return Uma lista de GrantedAuthority representando as funÃ§Ãµes formatadas.
     */
    @SuppressWarnings("unchecked")
    private Collection<? extends GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        Map<String, Object> realmAccess = jwt.getClaimAsMap(REALM_ACCESS);
        if (realmAccess == null || realmAccess.get(ROLES) == null) {
            return authorities;
        }

        List<String> roles = (List<String>) realmAccess.get(ROLES);
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        }

        return authorities;
    }

    /**
     * ObtÃ©m o nome da reivindicaÃ§Ã£o principal do token JWT.
     *
     * @param jwt O token de autenticaÃ§Ã£o JWT enviado na solicitaÃ§Ã£o.
     * @return O nome do usuÃ¡rio que gerou o token. Se o atributo
     *         'preferred_username' estiver definido,
     *         o valor desse atributo ser retornado. Caso contrÃ¡rio, o
     *         subidentificador do token ('sub') serÃ¡ retornado.
     */
    private String getPrincipleClaimName(Jwt jwt) {
        return Optional.of(jwt.getClaimAsString(PREFERRED_USERNAME))
                .orElse(jwt.getClaimAsString(SUB));
    }
}

