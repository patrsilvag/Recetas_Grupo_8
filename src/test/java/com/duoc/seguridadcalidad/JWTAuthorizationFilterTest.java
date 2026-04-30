package com.duoc.seguridadcalidad;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JWTAuthorizationFilterTest {

    @InjectMocks
    private JWTAuthorizationFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    // 🛡️ REQUISITO: Usar exactamente la misma llave que está en el filtro original
    private final String SECRET_KEY =
            "ZnJhc2VzbGFyZ2FzcGFyYWNvbG9jYXJjb21vY2xhdmVlbnVucHJvamVjdG9kZWVtZXBsb3BhcmFqd3Rjb25zcHJpbmdzZWN1cml0eQ==bWlwcnVlYmFkZWVqbXBsb3BhcmFiYXNlNjQ=";

    @BeforeEach
    @AfterEach
    void clearSecurityContext() {
        // Limpiamos la memoria antes y después de cada test
        SecurityContextHolder.clearContext();
    }

    // Método auxiliar para generar tokens válidos matemáticamente
    private String generarTokenValido(List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
        return Jwts.builder().subject("usuario_prueba").claim("authorities", roles)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 10)).signWith(key)
                .compact();
    }

    // ✅ Test 1: Petición anónima (Sin Header ni Cookie)
    @Test
    void doFilterInternal_SinToken_ContinuaVacio() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    // ✅ Test 2: Token enviado tradicionalmente en el Header
    @Test
    void doFilterInternal_ConTokenEnHeader_AsignaAutorizacion() throws Exception {
        String token = generarTokenValido(List.of("ROLE_ADMIN"));
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("usuario_prueba",
                SecurityContextHolder.getContext().getAuthentication().getName());
        verify(filterChain).doFilter(request, response);
    }

    // ✅ Test 3: Token enviado por Cookie (Crucial para Thymeleaf Front-end)
    @Test
    void doFilterInternal_ConTokenEnCookie_AsignaAutorizacion() throws Exception {
        String token = generarTokenValido(List.of("ROLE_USER"));

        // Simulamos que el header viene vacío, pero la cookie SÍ trae el token
        when(request.getHeader("Authorization")).thenReturn(null);
        Cookie cookie = new Cookie("token", token);
        when(request.getCookies()).thenReturn(new Cookie[] {cookie});

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    // ✅ Test 4: Token malformado o manipulado maliciosamente
    @Test
    void doFilterInternal_TokenInvalido_LimpiaContexto() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token-inventado-y-falso");

        filter.doFilterInternal(request, response, filterChain);

        // Debe caer al catch() y limpiar la sesión para proteger la app
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    // ✅ Test 5: Agrega el prefijo ROLE_ automáticamente si falta
    @Test
    void doFilterInternal_TokenSinPrefijoRole_AgregaPrefijo() throws Exception {
        // Le pasamos solo "ADMIN" en lugar de "ROLE_ADMIN"
        String token = generarTokenValido(List.of("ADMIN"));
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        // Validamos que el filtro lo arregló y le puso "ROLE_ADMIN"
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
}
