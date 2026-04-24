package com.duoc.seguridadcalidad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // 👈
                                                                                             // NECESARIO
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    // 🛡️ PASO 1: Inyectar el filtro (El que resolverás con las dependencias de JWT)
    @Autowired
    private JWTAuthorizationFilter jwtAuthorizationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .headers(headers -> headers.contentTypeOptions(withDefaults())
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; "
                                + "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; "
                                + "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net; "
                                + "img-src 'self' data: blob: http://localhost:8081 https:; "
                                + "connect-src 'self' http://localhost:8081;")))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/registro", "/recetas", "/", "/recetas.css",
                                "/login.css", "/js/**")
                        .permitAll().requestMatchers("/admin-usuarios").hasRole("ADMIN")
                        .requestMatchers("/home", "/crear-receta", "/detalle/**").authenticated()
                        .anyRequest().authenticated())

                // 🛡️ PASO 2: Registrar el filtro nativo
                // Esto permite que Java valide el token JWT antes de intentar redirigir al login
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)

                .formLogin(login -> login.loginPage("/login").permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/").permitAll());

        return http.build();
    }
}
