package com.duoc.seguridadcalidad;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    // ✅ MISMA LLAVE QUE PUSISTE EN application.properties
    private final String secretKey =
            "ZnJhc2VzbGFyZ2FzcGFyYWNvbG9jYXJjb21vY2xhdmVlbnVucHJvamVjdG9kZWVtZXBsb3BhcmFqd3Rjb25zcHJpbmdzZWN1cml0eQ==";

    @BeforeEach
    void setUp() throws Exception {
        // ✅ SOLUCIÓN AL NULL: Inyectamos manualmente el valor en el campo privado
        // Esto simula lo que hace @Value en el entorno de ejecución real
        java.lang.reflect.Field field = JWTAuthorizationFilter.class.getDeclaredField("jwtSecret");
        field.setAccessible(true);
        field.set(filter, secretKey);

        SecurityContextHolder.clearContext();
    }

    private String generarToken(List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
        return Jwts.builder().subject("test_user").claim("authorities", roles).issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000)).signWith(key).compact();
    }

    @Test
    void doFilterInternal_TokenSinPrefijoRole_AgregaPrefijo() throws Exception {
        String token = generarToken(List.of("ADMIN"));
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilterInternal(request, response, filterChain);

        // ✅ Ahora dejará de ser null porque el filtro ya tiene la llave para validar
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void doFilterInternal_ConHeaderVacio_ContinuaFiltro() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("");
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

}
