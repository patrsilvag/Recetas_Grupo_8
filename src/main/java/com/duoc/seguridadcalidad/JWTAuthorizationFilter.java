package com.duoc.seguridadcalidad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie; // ✅ Necesario para leer cookies
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JWTAuthorizationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret:ZnJhc2Vfbm9fcHJlZGVjaWJsZV9wYXJhX2V2aXRhcl9ibG9ja2VyX2RlX3NvbmFyXzIwMjY=}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. Intentar obtener el token del Header
        String token = recuperarTokenDeHeader(request);

        // 2. Si no hay Header (navegación normal), buscar en Cookies
        if (token == null) {
            token = recuperarTokenDeCookie(request);
        }

        // 3. Si encontramos un token, validamos y autenticamos
        if (token != null) {
            try {
                Claims claims = validateToken(token);
                if (claims.getSubject() != null) {
                    setUpSpringAuthentication(claims);
                }
            } catch (Exception e) {
                // Si el token es inválido o expiró, limpiamos el contexto
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String recuperarTokenDeHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.replace("Bearer ", "");
        }
        return null;
    }

    private String recuperarTokenDeCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                // ✅ Debe coincidir con el nombre usado en login.html: "token"
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    private void setUpSpringAuthentication(Claims claims) {
        List<?> authoritiesClaim = claims.get("authorities", List.class);
        List<String> roles = new ArrayList<>();

        if (authoritiesClaim != null) {
            for (Object obj : authoritiesClaim) {
                if (obj instanceof String role) {
                    if (!role.startsWith("ROLE_")) {
                        role = "ROLE_" + role;
                    }
                    roles.add(role);
                }
            }
        }

        if (roles.isEmpty())
            roles.add("ROLE_USER");

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
                        roles.stream().map(SimpleGrantedAuthority::new).toList());

        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
