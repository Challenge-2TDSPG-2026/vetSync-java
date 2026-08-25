package br.com.fiap.VetSync.controller;

import br.com.fiap.VetSync.entity.Tutor;
import br.com.fiap.VetSync.service.TutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tutores")
@RequiredArgsConstructor
@Tag(name = "Tutores", description = "Gerenciamento de tutores (donos dos pets)")
public class TutorController {

    private final TutorService tutorService;

    public record TutorAtualizarRequest(
            @NotBlank(message = "Nome é obrigatório")
            @Size(min = 2, max = 100)
            String nmTutor,

            @Pattern(regexp = "^\\d{10,11}$", message = "Telefone deve ter 10 ou 11 dígitos")
            String nrTelefone
    ) {}

    public record TutorResponse(
            Long idTutor,
            String nmTutor,
            String dsEmail,
            String nrTelefone,
            String dsCpf
    ) {}

    private TutorResponse toResponse(Tutor tutor) {
        return new TutorResponse(
                tutor.getIdTutor(),
                tutor.getNmTutor(),
                tutor.getDsEmail(),
                tutor.getNrTelefone(),
                tutor.getDsCpf()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tutor por ID")
    public TutorResponse buscarPorId(@PathVariable Long id) {
        return toResponse(tutorService.buscarPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar todos os tutores")
    public List<TutorResponse> listarTodos() {
        return tutorService.listarTodos().stream()
                .map(this::toResponse)
                .toList();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados do tutor (nome e telefone)")
    public TutorResponse atualizar(@PathVariable Long id, @RequestBody @Valid TutorAtualizarRequest request) {
        Tutor tutorAtualizado = Tutor.builder()
                .nmTutor(request.nmTutor())
                .nrTelefone(request.nrTelefone())
                .build();
        return toResponse(tutorService.atualizar(id, tutorAtualizado));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar tutor")
    public void deletar(@PathVariable Long id) {
        tutorService.deletar(id);
    }
}