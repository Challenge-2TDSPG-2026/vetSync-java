package br.com.fiap.JornadaPet.controller;

import br.com.fiap.JornadaPet.entity.Pet;
import br.com.fiap.JornadaPet.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
            String nome,
            String especie,
            String raca,
            Double peso,
            LocalDate dataNascimento,
            String sexo,
            boolean castrado,
            String observacoes
    ) {}


    public record PetResponse(
            Long id,
            String nome,
            String especie,
            String raca,
            Double peso,
            LocalDate dataNascimento,
            int idadeAnos,       // calculado automaticamente
            String sexo,
            boolean castrado,
            String observacoes,
            Long tutorId
    ) {}


    private PetResponse toResponse(Pet pet) {
        int idade = Period.between(pet.getDataNascimento(), LocalDate.now()).getYears();
        return new PetResponse(
                pet.getId(),
                pet.getNome(),
                pet.getEspecie(),
                pet.getRaca(),
                pet.getPeso(),
                pet.getDataNascimento(),
                idade,
                pet.getSexo(),
                pet.isCastrado(),
                pet.getObservacoes(),
                pet.getTutor() != null ? pet.getTutor().getId() : null
        );
    }

    @PostMapping("/tutor/{tutorId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar pet", description = "Cadastra um pet vinculado a um tutor. Eventos iniciais são sugeridos automaticamente.")
    @ApiResponse(responseCode = "201", description = "Pet cadastrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Tutor não encontrado")
    public PetResponse cadastrar(@PathVariable Long tutorId,
                                 @RequestBody @Valid PetRequest request) {
        Pet pet = Pet.builder()
                .nome(request.nome())
                .especie(request.especie())
                .raca(request.raca())
                .peso(request.peso())
                .dataNascimento(request.dataNascimento())
                .sexo(request.sexo())
                .castrado(request.castrado())
                .observacoes(request.observacoes())
                .build();
        return toResponse(petService.cadastrar(pet, tutorId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pet por ID", description = "Retorna perfil completo com idade calculada")
    @ApiResponse(responseCode = "200", description = "Pet encontrado")
    @ApiResponse(responseCode = "404", description = "Pet não encontrado")
    public PetResponse buscarPorId(@PathVariable Long id) {
        return toResponse(petService.buscarPorId(id));
    }

    @GetMapping("/tutor/{tutorId}")
    @Operation(summary = "Listar pets por tutor", description = "Suporta paginação e ordenação")
    public Page<PetResponse> listarPorTutor(
            @PathVariable Long tutorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nome") String sort) {
        return petService.listarPorTutor(tutorId,
                        PageRequest.of(page, size, Sort.by(sort)))
                .map(this::toResponse);
    }

    @GetMapping
    @Operation(summary = "Listar pets por espécie ou raça")
    public List<Pet> listarPorEspecie(@RequestParam(required = false) String especie) {
        if (especie != null) return petService.listarPorEspecie(especie);
        return petService.listarPorEspecie("cão"); // padrão
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar pet")
    @ApiResponse(responseCode = "200", description = "Pet atualizado")
    @ApiResponse(responseCode = "404", description = "Pet não encontrado")
    public PetResponse atualizar(@PathVariable Long id,
                                 @RequestBody PetRequest request) {
        Pet petAtualizado = Pet.builder()
                .nome(request.nome())
                .raca(request.raca())
                .peso(request.peso())
                .observacoes(request.observacoes())
                .castrado(request.castrado())
                .build();
        return toResponse(petService.atualizar(id, petAtualizado));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar pet")
    @ApiResponse(responseCode = "204", description = "Pet removido")
    @ApiResponse(responseCode = "404", description = "Pet não encontrado")
    public void deletar(@PathVariable Long id) {
        petService.deletar(id);
    }

}