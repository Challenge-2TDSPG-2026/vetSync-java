package br.com.fiap.VetSync.service;

import br.com.fiap.VetSync.entity.Clinica;
import br.com.fiap.VetSync.entity.Veterinario;
import br.com.fiap.VetSync.repository.ClinicaRepository;
import br.com.fiap.VetSync.repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VeterinarioService {

    private final VeterinarioRepository veterinarioRepository;
    private final ClinicaRepository clinicaRepository;
    private final PasswordEncoder passwordEncoder;

    public Veterinario cadastrar(String nome, String crmv, String email, String senhaBruta, Long idClinica) {
        if (veterinarioRepository.existsByDsEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
        }
        Clinica clinica = clinicaRepository.findById(idClinica)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clínica não encontrada"));

        Veterinario vet = Veterinario.builder()
                .nmVeterinario(nome)
                .nrCrmv(crmv)
                .dsEmail(email)
                .dsSenha(passwordEncoder.encode(senhaBruta))
                .clinica(clinica)
                .build();
        return veterinarioRepository.save(vet);
    }

    public Veterinario buscarPorId(Long id) {
        return veterinarioRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinário não encontrado com id: " + id)
        );
    }

    public List<Veterinario> listarTodos() {
        return veterinarioRepository.findAll();
    }

    public Veterinario atualizar(Long id, String nome, Long idClinica) {
        Veterinario vet = buscarPorId(id);
        if (nome != null && !nome.isBlank()) {
            vet.setNmVeterinario(nome);
        }
        if (idClinica != null) {
            Clinica clinica = clinicaRepository.findById(idClinica)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clínica não encontrada"));
            vet.setClinica(clinica);
        }
        return veterinarioRepository.save(vet);
    }
}