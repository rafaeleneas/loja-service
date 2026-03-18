package com.loja.lojaservice.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class WebClientInfo {
    private String id;
    private String clientId;
    private String clientSecret;
    private String authorizationGrantType;
    private String userNameAttribute;
    private String issuerUri;
    private String provider;
    private String tokenUri;
    private String scope;
}

