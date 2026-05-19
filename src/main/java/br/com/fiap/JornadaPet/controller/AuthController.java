package br.com.fiap.JornadaPet.controller;

import br.com.fiap.JornadaPet.entity.Tutor;
import br.com.fiap.JornadaPet.repository.TutorRepository;
import br.com.fiap.JornadaPet.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Login e registro de tutores")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final TutorRepository tutorRepository;
    private final PasswordEncoder passwordEncoder;

    public record LoginRequest(String email, String senha) {}
    public record RegisterRequest(String nome, String email, String senha, String telefone, String cpf) {}
    public record AuthResponse(String token, String email, String nome) {}

    @PostMapping("/login")
    @Operation(summary = "Login — retorna token JWT")
    public AuthResponse login(@RequestBody LoginRequest req) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.senha())
            );
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }
        var tutor = tutorRepository.findByEmail(req.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tutor não encontrado"));
        return new AuthResponse(jwtService.gerarToken(req.email()), tutor.getEmail(), tutor.getNome());
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar novo tutor com senha")
    public AuthResponse register(@RequestBody RegisterRequest req) {
        if (tutorRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado");
        }
        var tutor = Tutor.builder()
                .nome(req.nome())
                .email(req.email())
                .senha(passwordEncoder.encode(req.senha()))
                .telefone(req.telefone())
                .cpf(req.cpf())
                .build();
        tutorRepository.save(tutor);
        return new AuthResponse(jwtService.gerarToken(req.email()), req.email(), req.nome());
    }
}