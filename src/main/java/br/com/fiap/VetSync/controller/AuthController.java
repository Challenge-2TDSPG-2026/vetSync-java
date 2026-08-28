package br.com.fiap.VetSync.controller;

import br.com.fiap.VetSync.entity.Tutor;
import br.com.fiap.VetSync.entity.Veterinario;
import br.com.fiap.VetSync.repository.TutorRepository;
import br.com.fiap.VetSync.repository.VeterinarioRepository;
import br.com.fiap.VetSync.security.TokenBlacklist;
import br.com.fiap.VetSync.service.JwtService;
import br.com.fiap.VetSync.service.VeterinarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registro, login, logout e sessão do usuário")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final TutorRepository tutorRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final VeterinarioService veterinarioService;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklist tokenBlacklist;

    private static final int SENHA_MIN_LENGTH = 6;

    public record LoginRequest(String email, String senha) {}

    public record RegistrarRequest(
            String tipo,
            String nome,
            String email,
            String senha,
            String cpf,
            String telefone,
            String crmv,
            Long idClinica
    ) {}

    public record AuthResponse(String token, Long idUsuario, String email, String nome, String perfil) {}
    public record MeResponse(Long idUsuario, String email, String nome, String perfil) {}

    @PostMapping("/login")
    @Operation(summary = "Login — apenas e-mail e senha. Funciona para tutor ou veterinário.")
    public AuthResponse login(@RequestBody LoginRequest req) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.senha())
            );
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos");
        }
        return montarAuthResponse(req.email(), jwtService.gerarToken(req.email()));
    }

    @PostMapping("/registrar")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar novo usuário", description = "tipo: TUTOR ou VETERINARIO. Senha com no mínimo 6 caracteres.")
    public AuthResponse registrar(@RequestBody RegistrarRequest req) {
        if (req.senha() == null || req.senha().length() < SENHA_MIN_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A senha deve ter pelo menos " + SENHA_MIN_LENGTH + " caracteres");
        }
        if ("VETERINARIO".equalsIgnoreCase(req.tipo())) {
            Veterinario vet = veterinarioService.cadastrar(req.nome(), req.crmv(), req.email(), req.senha(), req.idClinica());
            return new AuthResponse(jwtService.gerarToken(req.email()), vet.getIdVeterinario(), req.email(), req.nome(), "VETERINARIO");
        }
        return registrarTutor(req);
    }

    private AuthResponse registrarTutor(RegistrarRequest req) {
        if (tutorRepository.existsByDsEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }
        var tutor = Tutor.builder()
                .nmTutor(req.nome())
                .dsEmail(req.email())
                .dsSenha(passwordEncoder.encode(req.senha()))
                .dsCpf(req.cpf())
                .nrTelefone(req.telefone())
                .build();
        tutor = tutorRepository.save(tutor);
        return new AuthResponse(jwtService.gerarToken(req.email()), tutor.getIdTutor(),
                req.email(), req.nome(), "TUTOR");
    }

    @PostMapping("/logout")
    @Operation(summary = "Invalidar o token atual", description = "Precisa estar autenticado; o token enviado deixa de funcionar a partir daqui.")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            tokenBlacklist.revogar(authHeader.substring(7));
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Dados do usuário autenticado (para restaurar a sessão no app)")
    public MeResponse me(Authentication authentication) {
        String email = authentication.getName();

        var tutor = tutorRepository.findByDsEmail(email);
        if (tutor.isPresent()) {
            return new MeResponse(tutor.get().getIdTutor(), email, tutor.get().getNmTutor(), "TUTOR");
        }
        var vet = veterinarioRepository.findByDsEmail(email);
        if (vet.isPresent()) {
            return new MeResponse(vet.get().getIdVeterinario(), email, vet.get().getNmVeterinario(), "VETERINARIO");
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
    }

    private AuthResponse montarAuthResponse(String email, String token) {
        var tutor = tutorRepository.findByDsEmail(email);
        if (tutor.isPresent()) {
            return new AuthResponse(token, tutor.get().getIdTutor(), email, tutor.get().getNmTutor(), "TUTOR");
        }
        var vet = veterinarioRepository.findByDsEmail(email);
        if (vet.isPresent()) {
            return new AuthResponse(token, vet.get().getIdVeterinario(), email, vet.get().getNmVeterinario(), "VETERINARIO");
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
    }
}