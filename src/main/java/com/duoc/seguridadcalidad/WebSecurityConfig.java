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
                            // 🔥 Deshabilitar CSRF (para facilitar pruebas y JWT)
                            .csrf(csrf -> csrf.disable())

                            .authorizeHttpRequests((requests) -> requests

                                            .requestMatchers(
                                                            "/", // /recetas/
                                                            "/buscar",
                                                            "/login",
                                                            "/detalle",
                                                            "/css/**",
                                                            "/js/**",
                                                            "/images/**",
                                                            "/**.css")
                                            .permitAll()

                                            .requestMatchers("/home", "/detalle")
                                            .authenticated()
                                            // 🔒 cualquier otra
                                            .anyRequest().authenticated())

                            // 🔐 LOGIN
                            .formLogin((form) -> form
                                            .loginPage("/login")
                                            .defaultSuccessUrl("/home", true)
                                            .permitAll())

                            // 🚪 LOGOUT
                            .logout((logout) -> logout
                                            .logoutSuccessUrl("/")
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

