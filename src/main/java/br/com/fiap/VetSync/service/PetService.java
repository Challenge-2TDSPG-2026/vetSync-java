package br.com.fiap.VetSync.service;

import br.com.fiap.VetSync.entity.Especie;
import br.com.fiap.VetSync.entity.EspecieCategoria;
import br.com.fiap.VetSync.entity.Pet;
import br.com.fiap.VetSync.entity.Raca;
import br.com.fiap.VetSync.entity.Tutor;
import br.com.fiap.VetSync.repository.EspecieRepository;
import br.com.fiap.VetSync.repository.PetRepository;
import br.com.fiap.VetSync.repository.RacaRepository;
import lombok.RequiredArgsConstructor;
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
    private final EspecieRepository especieRepository;
    private final RacaRepository racaRepository;


    public Pet cadastrar(Pet pet, Long idTutor, EspecieCategoria categoria, String especieOutro, String nmRaca) {
        Tutor tutor = tutorService.buscarPorId(idTutor);

        String nmEspecie = resolverNomeEspecie(categoria, especieOutro);

        Especie especie = especieRepository.findByNmEspecieIgnoreCase(nmEspecie)
                .orElseGet(() -> especieRepository.save(
                        Especie.builder().nmEspecie(nmEspecie).build()
                ));

        Raca raca = racaRepository.findByNmRacaIgnoreCaseAndEspecie_IdEspecie(nmRaca.trim(), especie.getIdEspecie())
                .orElseGet(() -> racaRepository.save(
                        Raca.builder().nmRaca(capitalizar(nmRaca)).especie(especie).build()
                ));

        pet.setTutor(tutor);
        pet.setRaca(raca);
        return petRepository.save(pet);
    }

    private String resolverNomeEspecie(EspecieCategoria categoria, String especieOutro) {
        if (categoria == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Espécie é obrigatória");
        }
        if (categoria != EspecieCategoria.OUTRO) {
            return categoria.getNomeOficial();
        }
        if (especieOutro == null || especieOutro.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe o nome da espécie quando escolher \"Outro\"");
        }
        return capitalizar(especieOutro);
    }

    private String capitalizar(String texto) {
        String t = texto.trim();
        if (t.isEmpty()) return t;
        return Character.toUpperCase(t.charAt(0)) + t.substring(1).toLowerCase();
    }

    public Pet buscarPorId(Long id) {
        return petRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet não encontrado com id: " + id)
        );
    }

    public List<Pet> listarPorTutor(Long idTutor) {
        return petRepository.findByTutor_IdTutor(idTutor);
    }

    public Pet atualizar(Long id, Pet petAtualizado) {
        Pet pet = buscarPorId(id);
        pet.setNmPet(petAtualizado.getNmPet());
        pet.setNrPesoKg(petAtualizado.getNrPesoKg());
        return petRepository.save(pet);
    }

    public void deletar(Long id) {
        Pet pet = buscarPorId(id);
        petRepository.delete(pet);
    }

    public int calcularIdade(Pet pet) {
        return Period.between(pet.getDtNascimento(), LocalDate.now()).getYears();
    }
}