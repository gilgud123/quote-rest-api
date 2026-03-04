package com.katya.quoterestapi.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@Profile("!test")
public class SecurityConfig {

  private final String keycloakClientId;
  private final String issuerUri;
  private final String jwkSetUri;
  private final String acceptedIssuers;

  public SecurityConfig(
      @Value("${app.security.keycloak.client-id:quote-api}") String keycloakClientId,
      @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}") String issuerUri,
      @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}") String jwkSetUri,
      @Value("${app.security.keycloak.accepted-issuers:}") String acceptedIssuers) {
    this.keycloakClientId = keycloakClientId;
    this.issuerUri = issuerUri;
    this.jwkSetUri = jwkSetUri;
    this.acceptedIssuers = acceptedIssuers;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/actuator/health")
                    .permitAll()
                    .requestMatchers(
                        "/swagger-ui.html", "/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/**")
                    .hasAnyRole("USER", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/v1/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/api/v1/**")
                    .hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/actuator/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            oauth2 ->
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

    return http.build();
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter(keycloakClientId));
    return converter;
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    Set<String> issuerSet = parseIssuers(acceptedIssuers);
    if (issuerUri != null && !issuerUri.isBlank()) {
      issuerSet.add(issuerUri);
    }

    if (jwkSetUri == null || jwkSetUri.isBlank()) {
      return JwtDecoders.fromIssuerLocation(issuerUri);
    }

    NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    OAuth2TokenValidator<Jwt> defaultValidator = JwtValidators.createDefault();
    OAuth2TokenValidator<Jwt> issuerValidator = new AllowedIssuerValidator(issuerSet);
    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(defaultValidator, issuerValidator));
    return decoder;
  }

  private Set<String> parseIssuers(String issuersValue) {
    if (issuersValue == null || issuersValue.isBlank()) {
      return new HashSet<>();
    }

    Set<String> issuers = new HashSet<>();
    Arrays.stream(issuersValue.split(","))
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .forEach(issuers::add);
    return issuers;
  }
}
