package com.duoc.seguridadcalidad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final JWTAuthorizationFilter jwtAuthorizationFilter;

    public WebSecurityConfig(JWTAuthorizationFilter jwtAuthorizationFilter) {
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        try {
            http
                    // 1. JWT stateless: no se utilizan sesiones ni cookies
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(session -> session
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                    // 2. Cabeceras de seguridad reforzadas (Corrección OWASP ZAP)
                    .headers(headers -> headers.contentTypeOptions(withDefaults())
                           .contentSecurityPolicy(csp -> csp.policyDirectives(
                                    "default-src 'self'; "
                                    + "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net http://gc.kis.v2.scr.kaspersky-labs.com; " 
                                    + "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net http://gc.kis.v2.scr.kaspersky-labs.com; " 
                                    + "img-src 'self' data: blob: https:; "
                                    + "connect-src 'self' http://localhost:8081 ws://gc.kis.v2.scr.kaspersky-labs.com; "
                                    + "object-src 'none'; "
                                    + "base-uri 'self'; "
                                    + "frame-ancestors 'none'; "
                                    + "form-action 'self';"
                            )))

                    // 3. Autorización de rutas
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/login", "/registro", "/recetas", "/", "/recetas.css",
                                    "/login.css", "/js/**")
                            .permitAll().requestMatchers("/admin-usuarios").hasRole("ADMIN")
                            .requestMatchers("/home", "/crear-receta", "/detalle/**", "/buscar")
                            .authenticated().anyRequest().authenticated())

                    // 4. Filtros y Login
                    .addFilterBefore(jwtAuthorizationFilter,
                            UsernamePasswordAuthenticationFilter.class)
                    .formLogin(login -> login.loginPage("/login").permitAll())
                    .logout(logout -> logout.logoutSuccessUrl("/").permitAll());

            return http.build();

        } catch (Exception e) {
            // ✅ Mantiene Rating A en Maintainability al no usar throws Exception
            throw new IllegalStateException("Error configurando la cadena de seguridad", e);
        }
    }
}
