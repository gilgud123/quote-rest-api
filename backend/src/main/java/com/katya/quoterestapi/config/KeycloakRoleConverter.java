package com.katya.quoterestapi.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

  private static final String ROLE_PREFIX = "ROLE_";

  private final String clientId;

  public KeycloakRoleConverter(String clientId) {
    this.clientId = clientId;
  }

  @Override
  public Collection<GrantedAuthority> convert(Jwt jwt) {
    List<GrantedAuthority> authorities = new ArrayList<>();

    authorities.addAll(extractRealmRoles(jwt));
    authorities.addAll(extractClientRoles(jwt));

    return authorities;
  }

  private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
    Map<String, Object> realmAccess = jwt.getClaim("realm_access");
    if (realmAccess == null) {
      return List.of();
    }

    Object roles = realmAccess.get("roles");
    if (!(roles instanceof Collection<?> roleList)) {
      return List.of();
    }

    return roleList.stream()
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(ROLE_PREFIX + role))
        .toList();
  }

  private Collection<GrantedAuthority> extractClientRoles(Jwt jwt) {
    Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
    if (resourceAccess == null) {
      return List.of();
    }

    Object clientAccess = resourceAccess.get(clientId);
    if (!(clientAccess instanceof Map<?, ?> clientAccessMap)) {
      return List.of();
    }

    Object roles = clientAccessMap.get("roles");
    if (!(roles instanceof Collection<?> roleList)) {
      return List.of();
    }

    return roleList.stream()
        .filter(String.class::isInstance)
        .map(String.class::cast)
        .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(ROLE_PREFIX + role))
        .toList();
  }
}
