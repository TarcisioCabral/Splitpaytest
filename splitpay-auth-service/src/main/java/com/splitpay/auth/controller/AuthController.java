package com.splitpay.auth.controller;

import com.splitpay.auth.config.JwtUtils;
import com.splitpay.auth.dto.AuthRequest;
import com.splitpay.auth.dto.AuthResponse;
import com.splitpay.auth.dto.RegisterRequest;
import com.splitpay.auth.model.User;
import com.splitpay.auth.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Tentativa de registro para o email: {}", request.getEmail());
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Tentativa de registro com email já existente: {}", request.getEmail());
            return ResponseEntity.badRequest().body("Email já está em uso");
        }
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : "ROLE_CLIENTE")
                .build();

        userRepository.save(user);
        log.info("Usuário registrado com sucesso: {}", request.getEmail());
        return ResponseEntity.ok("Usuário registrado com sucesso");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        log.info("Tentativa de login para o email: {}", request.getEmail());
        return authenticate(request.getEmail(), request.getPassword());
    }

    @PostMapping("/token")
    public ResponseEntity<AuthResponse> token(@RequestBody AuthRequest request) {
        log.info("Tentativa de obtenção de token para o email: {}", request.getEmail());
        return authenticate(request.getEmail(), request.getPassword());
    }

    private ResponseEntity<AuthResponse> authenticate(String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtUtils.generateToken(userDetails);

            User user = userRepository.findByEmail(email).orElseThrow(() -> {
                log.error("Usuário autenticado mas não encontrado no banco: {}", email);
                return new RuntimeException("Usuário não encontrado");
            });

            log.info("Login bem-sucedido para o email: {}", email);
            return ResponseEntity.ok(AuthResponse.builder()
                    .token(jwt)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .build());
        } catch (Exception e) {
            log.error("Falha na autenticação para o email: {}. Erro: {}", email, e.getMessage());
            return ResponseEntity.status(403).build();
        }
    }
}
