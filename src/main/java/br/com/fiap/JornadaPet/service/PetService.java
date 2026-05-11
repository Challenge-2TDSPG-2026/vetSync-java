package br.com.fiap.JornadaPet.service;

import br.com.fiap.JornadaPet.entity.EventoSaude;
import br.com.fiap.JornadaPet.entity.EventoSaude.TipoEvento;
import br.com.fiap.JornadaPet.entity.Pet;
import br.com.fiap.JornadaPet.entity.Tutor;
import br.com.fiap.JornadaPet.repository.EventoSaudeRepository;
import br.com.fiap.JornadaPet.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepository;
    private final TutorService tutorService;
    private final EventoSaudeRepository eventoSaudeRepository;


    public Pet cadastrar(Pet pet, Long tutorId) {
        Tutor tutor = tutorService.buscarPorId(tutorId);
        pet.setTutor(tutor);
        Pet petSalvo = petRepository.save(pet);
        sugerirEventosIniciais(petSalvo);
        return petSalvo;
    }

    public Pet buscarPorId(Long id) {
        return petRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Pet não encontrado com id: " + id)
        );
    }


    public Page<Pet> listarPorTutor(Long tutorId, Pageable pageable) {
        return petRepository.findByTutorId(tutorId, pageable);
    }


    public List<Pet> listarPorEspecie(String especie) {
        return petRepository.findByEspecie(especie);
    }


    public Pet atualizar(Long id, Pet petAtualizado) {
        Pet pet = buscarPorId(id);
        pet.setNome(petAtualizado.getNome());
        pet.setPeso(petAtualizado.getPeso());
        pet.setRaca(petAtualizado.getRaca());
        pet.setObservacoes(petAtualizado.getObservacoes());
        pet.setCastrado(petAtualizado.isCastrado());
        return petRepository.save(pet);
    }


    public void deletar(Long id) {
        Pet pet = buscarPorId(id);
        petRepository.delete(pet);
    }

    public int calcularIdade(Pet pet) {
        return Period.between(pet.getDataNascimento(), LocalDate.now()).getYears();
    }


    private void sugerirEventosIniciais(Pet pet) {
        int idade = calcularIdade(pet);

        if (idade < 1) {

            eventoSaudeRepository.save(EventoSaude.builder()
                    .pet(pet)
                    .tipo(TipoEvento.VACINA)
                    .descricao("Série inicial de vacinas - filhote")
                    .dataProxima(LocalDate.now().plusDays(30))
                    .build());

            eventoSaudeRepository.save(EventoSaude.builder()
                    .pet(pet)
                    .tipo(TipoEvento.VERMIFUGO)
                    .descricao("Vermifugação inicial")
                    .dataProxima(LocalDate.now().plusDays(15))
                    .build());
        } else {

            eventoSaudeRepository.save(EventoSaude.builder()
                    .pet(pet)
                    .tipo(TipoEvento.CHECKUP)
                    .descricao("Check-up anual recomendado")
                    .dataProxima(LocalDate.now().plusMonths(12))
                    .build());

            eventoSaudeRepository.save(EventoSaude.builder()
                    .pet(pet)
                    .tipo(TipoEvento.VERMIFUGO)
                    .descricao("Vermifugação semestral")
                    .dataProxima(LocalDate.now().plusMonths(6))
                    .build());
        }
    }

}