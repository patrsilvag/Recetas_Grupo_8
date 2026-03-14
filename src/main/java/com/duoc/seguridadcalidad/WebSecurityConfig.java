package com.duoc.seguridadcalidad;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.context.annotation.Description;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                        .authorizeHttpRequests((requests) -> requests
                                        // ELIMINAMOS "/home" de esta línea para que no sea pública
                                        .requestMatchers("/", "/buscar", "/login", "/css/**", "/js/**", "/images/**",
                                                        "/**.css")
                                        .permitAll()

                                        // Al no estar arriba, esta línea atrapará a "/home" y exigirá login
                                        .anyRequest().authenticated())
                .formLogin((form) -> form
                        .loginPage("/login") // Nuestra página de login personalizada [cite: 747]
                        .defaultSuccessUrl("/home", true) // Redirige al inicio tras loguearse
                        .permitAll())
                .logout((logout) -> logout
                                        .logoutSuccessUrl("/") // Redirige a la raíz pública tras salir
                        .permitAll());

        return http.build();
    }

    
    @Bean
    @Description("In memory Userdetails service registered since DB doesn't have user table ")
    public UserDetailsService users() {
        // The builder will ensure the passwords are encoded before saving in memory
        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("password"))
                .roles("USER")
                .build();
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("password"))
                .roles("USER", "ADMIN")
                .build();
        // TERCER USUARIO REQUERIDO POR LA RÚBRICA
        UserDetails chef = User.builder()
                .username("chef")
                .password(passwordEncoder().encode("password"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user, admin, chef);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

