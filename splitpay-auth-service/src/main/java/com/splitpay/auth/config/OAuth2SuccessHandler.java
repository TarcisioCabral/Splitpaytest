package com.splitpay.auth.config;

import com.splitpay.auth.model.User;
import com.splitpay.auth.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @Value("${splitpay.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        log.info("OAuth2 login bem-sucedido para o email: {}", email);

        // Busca ou cria o usuário
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            log.info("Criando novo usuário via OAuth2: {}", email);
            User newUser = User.builder()
                    .email(email)
                    .username(name)
                    .password("") // Sem senha para usuários OAuth2
                    .role("ROLE_CLIENTE")
                    .build();
            return userRepository.save(newUser);
        });

        // Gera o token JWT
        // Usamos uma implementação simples do UserDetails baseada no nosso model
        org.springframework.security.core.userdetails.UserDetails userDetails = 
            org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password("")
                .authorities(user.getRole())
                .build();
        
        String token = jwtUtils.generateToken(userDetails);

        // Redireciona para o frontend com o token na query string
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/dashboard")
                .queryParam("token", token)
                .queryParam("email", user.getEmail())
                .queryParam("username", user.getUsername())
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
