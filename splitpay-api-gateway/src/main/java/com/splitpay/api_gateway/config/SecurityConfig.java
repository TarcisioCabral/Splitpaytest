package com.splitpay.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança do API Gateway.
 *
 * O Gateway atua como roteador/proxy transparente — ele repassa o header
 * Authorization para os microserviços downstream, mas NÃO valida tokens JWT
 * aqui. A autenticação real é responsabilidade de cada serviço.
 *
 * Rotas públicas (leitura de dashboard) são permitidas sem autenticação.
 * Rotas de escrita exigem que o cliente envie um Bearer token, que será
 * validado pelo microserviço de destino.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Infraestrutura
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/fallback").permitAll()

                // Leituras públicas do dashboard (sem necessidade de login)
                .requestMatchers(HttpMethod.GET, "/v1/split/recent").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/split/stream/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/aliquota/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/v1/simulador/**").permitAll()

                // Todo o resto é permitido — autenticação delegada aos microserviços
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
