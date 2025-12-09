package io.github.eventify.common.security.converter;

import io.github.eventify.api.user.model.User;
import io.github.eventify.api.user.service.UserService;
import io.github.eventify.common.security.principal.JwtUserPrincipalAuthenticationToken;
import io.github.eventify.common.security.principal.UserTokenPrincipal;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

/**
 * Converts a {@link Jwt} into a {@link JwtUserPrincipalAuthenticationToken}.
 */
@Data
@Component
@RequiredArgsConstructor
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserService userService;

    private final Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    /**
     * Converts a {@link Jwt} into a {@link JwtUserPrincipalAuthenticationToken}. Which basically wraps the {@link Jwt} and the {@link User}
     * into a {@link UserTokenPrincipal}. Meaning that the {@link Jwt} is the token and the {@link User} is the owner of that token.
     */
    @Override
    public JwtUserPrincipalAuthenticationToken convert(final Jwt jwt) {
        final User user = userService.loadUserByUsername(jwt.getSubject());
        final UserTokenPrincipal principal = new UserTokenPrincipal(user, jwt);
        final Collection<GrantedAuthority> authorities = jwtGrantedAuthoritiesConverter.convert(jwt);
        return new JwtUserPrincipalAuthenticationToken(principal, authorities);
    }
}
