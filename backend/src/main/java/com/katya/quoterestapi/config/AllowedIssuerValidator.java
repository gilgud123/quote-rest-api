package com.katya.quoterestapi.config;

import java.util.Set;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class AllowedIssuerValidator implements OAuth2TokenValidator<Jwt> {

  private final Set<String> allowedIssuers;
  private final Set<String> normalizedIssuers;

  public AllowedIssuerValidator(Set<String> allowedIssuers) {
    this.allowedIssuers = allowedIssuers;
    this.normalizedIssuers = normalizeIssuers(allowedIssuers);
  }

  @Override
  public OAuth2TokenValidatorResult validate(Jwt token) {
    if (allowedIssuers == null || allowedIssuers.isEmpty()) {
      return OAuth2TokenValidatorResult.success();
    }

    java.net.URL issuer = token.getIssuer();
    String issuerValue = issuer == null ? null : issuer.toString();
    if (issuerValue != null && isAllowedIssuer(issuerValue)) {
      return OAuth2TokenValidatorResult.success();
    }

    OAuth2Error error = new OAuth2Error("invalid_token", "Token issuer is not allowed", null);
    return OAuth2TokenValidatorResult.failure(error);
  }

  private boolean isAllowedIssuer(String issuerValue) {
    if (allowedIssuers.contains(issuerValue)) {
      return true;
    }

    String normalized = normalizeIssuer(issuerValue);
    return normalizedIssuers.contains(normalized);
  }

  private Set<String> normalizeIssuers(Set<String> issuers) {
    if (issuers == null || issuers.isEmpty()) {
      return java.util.Collections.emptySet();
    }

    java.util.Set<String> normalized = new java.util.HashSet<>();
    for (String issuer : issuers) {
      if (issuer != null && !issuer.isBlank()) {
        normalized.add(normalizeIssuer(issuer));
      }
    }
    return normalized;
  }

  private String normalizeIssuer(String issuer) {
    String value = issuer.trim();
    while (value.endsWith("/")) {
      value = value.substring(0, value.length() - 1);
    }
    return value;
  }
}
