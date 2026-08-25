package br.com.fiap.VetSync.controller;

import br.com.fiap.VetSync.entity.Clinica;
import br.com.fiap.VetSync.entity.Tutor;
import br.com.fiap.VetSync.entity.Veterinario;
import br.com.fiap.VetSync.repository.ClinicaRepository;
import br.com.fiap.VetSync.repository.TutorRepository;
import br.com.fiap.VetSync.repository.VeterinarioRepository;
import br.com.fiap.VetSync.service.JwtService;
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
@Tag(name = "Auth", description = "Login e registro de tutores e veterinários")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final TutorRepository tutorRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final ClinicaRepository clinicaRepository;
    private final PasswordEncoder passwordEncoder;

    public record LoginRequest(String email, String senha) {}

    public record RegisterTutorRequest(
            String nmTutor, String dsEmail, String dsSenha,
            String nrTelefone, String dsCpf
    ) {}

    public record RegisterVeterinarioRequest(
            String nmVeterinario, String nrCrmv, String dsEmail,
            String dsSenha, Long idClinica
    ) {}

    public record AuthResponse(String token, String email, String nome, String perfil) {}

    @PostMapping("/login")
    @Operation(summary = "Login — funciona para tutor ou veterinário, retorna token JWT")
    public AuthResponse login(@RequestBody LoginRequest req) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.senha())
            );
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }

        var tutor = tutorRepository.findByDsEmail(req.email());
        if (tutor.isPresent()) {
            return new AuthResponse(jwtService.gerarToken(req.email()), tutor.get().getDsEmail(),
                    tutor.get().getNmTutor(), "TUTOR");
        }

        var vet = veterinarioRepository.findByDsEmail(req.email());
        if (vet.isPresent()) {
            return new AuthResponse(jwtService.gerarToken(req.email()), vet.get().getDsEmail(),
                    vet.get().getNmVeterinario(), "VETERINARIO");
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar novo tutor com senha")
    public AuthResponse register(@RequestBody RegisterTutorRequest req) {
        if (tutorRepository.existsByDsEmail(req.dsEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }
        var tutor = Tutor.builder()
                .nmTutor(req.nmTutor())
                .dsEmail(req.dsEmail())
                .dsSenha(passwordEncoder.encode(req.dsSenha()))
                .nrTelefone(req.nrTelefone())
                .dsCpf(req.dsCpf())
                .build();
        tutorRepository.save(tutor);
        return new AuthResponse(jwtService.gerarToken(req.dsEmail()), req.dsEmail(), req.nmTutor(), "TUTOR");
    }

    @PostMapping("/register/veterinario")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar novo veterinário com senha", description = "Requer uma clínica já cadastrada (idClinica)")
    public AuthResponse registerVeterinario(@RequestBody RegisterVeterinarioRequest req) {
        if (veterinarioRepository.existsByDsEmail(req.dsEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }
        Clinica clinica = clinicaRepository.findById(req.idClinica())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clínica não encontrada"));

        var vet = Veterinario.builder()
                .nmVeterinario(req.nmVeterinario())
                .nrCrmv(req.nrCrmv())
                .dsEmail(req.dsEmail())
                .dsSenha(passwordEncoder.encode(req.dsSenha()))
                .clinica(clinica)
                .build();
        veterinarioRepository.save(vet);
        return new AuthResponse(jwtService.gerarToken(req.dsEmail()), req.dsEmail(), req.nmVeterinario(), "VETERINARIO");
    }
}