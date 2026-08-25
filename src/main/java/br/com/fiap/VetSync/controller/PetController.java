package br.com.fiap.VetSync.controller;

import br.com.fiap.VetSync.entity.EspecieCategoria;
import br.com.fiap.VetSync.entity.Pet;
import br.com.fiap.VetSync.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@RestController
@RequestMapping("/pets")
@RequiredArgsConstructor
@Tag(name = "Pets", description = "Cadastro e gerenciamento de pets")
public class PetController {

    private final PetService petService;

    public record PetRequest(
            @NotBlank(message = "Nome do pet é obrigatório")
            String nmPet,

            @NotNull(message = "Espécie é obrigatória")
            EspecieCategoria especie,

            String especieOutro,

            @NotBlank(message = "Raça é obrigatória")
            String raca,

            @NotNull(message = "Data de nascimento é obrigatória")
            LocalDate dtNascimento,

            BigDecimal peso
    ) {}

    public record PetResponse(
            Long idPet,
            String nmPet,
            String especie,
            String raca,
            LocalDate dtNascimento,
            int idadeAnos,
            BigDecimal peso,
            Long idTutor
    ) {}

    private PetResponse toResponse(Pet pet) {
        int idade = Period.between(pet.getDtNascimento(), LocalDate.now()).getYears();
        return new PetResponse(
                pet.getIdPet(),
                pet.getNmPet(),
                pet.getRaca() != null && pet.getRaca().getEspecie() != null
                        ? pet.getRaca().getEspecie().getNmEspecie() : null,
                pet.getRaca() != null ? pet.getRaca().getNmRaca() : null,
                pet.getDtNascimento(),
                idade,
                pet.getNrPesoKg(),
                pet.getTutor() != null ? pet.getTutor().getIdTutor() : null
        );
    }

    @PostMapping("/tutor/{idTutor}")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('TUTOR')")
    @Operation(summary = "Cadastrar pet", description = "Somente TUTOR. especie: CAO, GATO, AVE ou OUTRO. Se OUTRO, preencha especieOutro com o nome digitado.")
    public PetResponse cadastrar(@PathVariable Long idTutor,
                                 @RequestBody @Valid PetRequest request) {
        Pet pet = Pet.builder()
                .nmPet(request.nmPet())
                .dtNascimento(request.dtNascimento())
                .nrPesoKg(request.peso())
                .build();
        return toResponse(petService.cadastrar(pet, idTutor, request.especie(), request.especieOutro(), request.raca()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pet por ID")
    public PetResponse buscarPorId(@PathVariable Long id) {
        return toResponse(petService.buscarPorId(id));
    }

    @GetMapping("/tutor/{idTutor}")
    @Operation(summary = "Listar pets por tutor")
    public List<PetResponse> listarPorTutor(@PathVariable Long idTutor) {
        return petService.listarPorTutor(idTutor).stream()
                .map(this::toResponse)
                .toList();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TUTOR')")
    @Operation(summary = "Atualizar pet (nome e peso). Somente TUTOR.")
    public PetResponse atualizar(@PathVariable Long id, @RequestBody PetRequest request) {
        Pet petAtualizado = Pet.builder()
                .nmPet(request.nmPet())
                .nrPesoKg(request.peso())
                .build();
        return toResponse(petService.atualizar(id, petAtualizado));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('TUTOR')")
    @Operation(summary = "Deletar pet. Somente TUTOR.")
    public void deletar(@PathVariable Long id) {
        petService.deletar(id);
    }
}