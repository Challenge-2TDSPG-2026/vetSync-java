package br.com.fiap.JornadaPet.controller;

import br.com.fiap.JornadaPet.entity.Tutor;
import br.com.fiap.JornadaPet.service.TutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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

    
    public record TutorRequest(
            @NotBlank(message = "Nome é obrigatório")
            @Size(min = 2, max = 100)
            String nome,

            @NotBlank(message = "E-mail é obrigatório")
            @Email(message = "E-mail inválido")
            String email,

            @Pattern(regexp = "^\\d{10,11}$", message = "Telefone deve ter 10 ou 11 dígitos")
            String telefone,

            @NotBlank(message = "CPF é obrigatório")
            @Pattern(regexp = "^\\d{11}$", message = "CPF deve ter 11 dígitos")
            String cpf
    ) {}

    public record TutorResponse(
            Long id,
            String nome,
            String email,
            String telefone,
            String cpf
    ) {}

    private TutorResponse toResponse(Tutor tutor) {
        return new TutorResponse(
                tutor.getId(),
                tutor.getNome(),
                tutor.getEmail(),
                tutor.getTelefone(),
                tutor.getCpf()
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar tutor", description = "Cria um novo tutor no sistema")
    @ApiResponse(responseCode = "201", description = "Tutor criado com sucesso")
    @ApiResponse(responseCode = "409", description = "Email já cadastrado")
    public TutorResponse cadastrar(@RequestBody @Valid TutorRequest request) {
        Tutor tutor = Tutor.builder()
                .nome(request.nome())
                .email(request.email())
                .telefone(request.telefone())
                .cpf(request.cpf())
                .build();
        return toResponse(tutorService.cadastrar(tutor));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tutor por ID")
    @ApiResponse(responseCode = "200", description = "Tutor encontrado")
    @ApiResponse(responseCode = "404", description = "Tutor não encontrado")
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
    @Operation(summary = "Atualizar tutor")
    @ApiResponse(responseCode = "200", description = "Tutor atualizado")
    @ApiResponse(responseCode = "404", description = "Tutor não encontrado")
    public TutorResponse atualizar(@PathVariable Long id, @RequestBody @Valid TutorRequest request) {
        Tutor tutorAtualizado = Tutor.builder()
                .nome(request.nome())
                .email(request.email())
                .telefone(request.telefone())
                .cpf(request.cpf())
                .build();
        return toResponse(tutorService.atualizar(id, tutorAtualizado));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar tutor")
    @ApiResponse(responseCode = "204", description = "Tutor removido")
    @ApiResponse(responseCode = "404", description = "Tutor não encontrado")
    public void deletar(@PathVariable Long id) {
        tutorService.deletar(id);
    }

}
