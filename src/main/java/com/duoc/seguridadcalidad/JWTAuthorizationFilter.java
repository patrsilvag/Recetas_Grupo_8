package com.duoc.seguridadcalidad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;

@Component
public class JWTAuthorizationFilter extends OncePerRequestFilter {

    // IMPORTANTE: Esta llave debe ser IDÉNTICA a la del Backend
    private final String SECRET_KEY = 
            "ZnJhc2VzbGFyZ2FzcGFyYWNvbG9jYXJjb21vY2xhdmVlbnVucHJvamVjdG9kZWVtZXBsb3BhcmFqd3Rjb25zcHJpbmdzZWN1cml0eQ==bWlwcnVlYmFkZWVqbXBsb3BhcmFiYXNlNjQ=";

    // Dentro de JWTAuthorizationFilter.java del Frontend
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = null;

        // 1. Intentar obtener el token del Header (para llamadas API directas)
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            token = header.replace("Bearer ", "");
        }

        // 2. 🍪 CRÍTICO: Intentar obtener el token de la Cookie (para la navegación GET)
        if (token == null && request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 3. Si encontramos un token, lo validamos y cargamos la identidad en Java
        if (token != null) {
            try {
                Claims claims = validateToken(token); // Asegúrate de que la SECRET_KEY sea la misma
                if (claims.get("authorities") != null) {
                    setUpSpringAuthentication(claims);
                }
            } catch (Exception e) {
                // Si el token es inválido o expiró, limpiamos y dejamos que Spring Security bloquee
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    private void setUpSpringAuthentication(Claims claims) {
        Object authoritiesClaim = claims.get("authorities");
        java.util.List<String> authorities = new java.util.ArrayList<>();

        if (authoritiesClaim instanceof java.util.List<?>) {
            for (Object obj : (java.util.List<?>) authoritiesClaim) {
                if (obj instanceof String) {
                    String role = (String) obj;
                    // 🛡️ : Asegurar el prefijo ROLE_ para que hasRole('ADMIN') funcione
                    if (!role.startsWith("ROLE_")) {
                        role = "ROLE_" + role;
                    }
                    authorities.add(role);
                }
            }
        }
        if (authorities.isEmpty()) {
            authorities.add("ROLE_USER");
        }

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                claims.getSubject(), null,
                authorities.stream().map(
                        org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                        .collect(java.util.stream.Collectors.toList()));

        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(auth);
    }
}
