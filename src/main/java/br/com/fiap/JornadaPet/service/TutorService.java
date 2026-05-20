package br.com.fiap.JornadaPet.service;
import br.com.fiap.JornadaPet.entity.Tutor;
import br.com.fiap.JornadaPet.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TutorService {
    private final TutorRepository tutorRepository;

    public Tutor cadastrar(Tutor tutor) {
        tutorRepository.findByEmail(tutor.getEmail()).ifPresent(t -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe um tutor com o email: " + tutor.getEmail());
        });
        return tutorRepository.save(tutor);
    }

    public Optional<Tutor> buscarPorEmail(String email) {
        return tutorRepository.findByEmail(email);
    }

    @Cacheable(value = "tutores", key = "#id")
    public Tutor buscarPorId(Long id) {
        return tutorRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Tutor não encontrado com id: " + id)
        );
    }

    public List<Tutor> listarTodos() {
        return tutorRepository.findAll();
    }

    @CacheEvict(value = "tutores", key = "#id")
    public Tutor atualizar(Long id, Tutor tutorAtualizado) {
        Tutor tutor = buscarPorId(id);
        tutor.setNome(tutorAtualizado.getNome());
        tutor.setEmail(tutorAtualizado.getEmail());
        tutor.setTelefone(tutorAtualizado.getTelefone());
        tutor.setCpf(tutorAtualizado.getCpf());
        return tutorRepository.save(tutor);
    }

    @CacheEvict(value = "tutores", key = "#id")
    public void deletar(Long id) {
        Tutor tutor = buscarPorId(id);
        tutorRepository.delete(tutor);
    }
}