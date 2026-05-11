package br.com.fiap.JornadaPet.controller;

import br.com.fiap.JornadaPet.entity.Tutor;
import br.com.fiap.JornadaPet.service.TutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    // DTO como record — padrão do professor
    public record TutorRequest(
            String nome,
            String email,
            String telefone,
            String cpf
    ) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar tutor", description = "Cria um novo tutor no sistema")
    @ApiResponse(responseCode = "201", description = "Tutor criado com sucesso")
    @ApiResponse(responseCode = "409", description = "Email já cadastrado")
    public Tutor cadastrar(@RequestBody @Valid TutorRequest request) {
        return tutorService.cadastrar(Tutor.builder()
                .nome(request.nome())
                .email(request.email())
                .telefone(request.telefone())
                .cpf(request.cpf())
                .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tutor por ID")
    @ApiResponse(responseCode = "200", description = "Tutor encontrado")
    @ApiResponse(responseCode = "404", description = "Tutor não encontrado")
    public Tutor buscarPorId(@PathVariable Long id) {
        return tutorService.buscarPorId(id);
    }

    @GetMapping
    @Operation(summary = "Listar todos os tutores")
    public List<Tutor> listarTodos() {
        return tutorService.listarTodos();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tutor")
    @ApiResponse(responseCode = "200", description = "Tutor atualizado")
    @ApiResponse(responseCode = "404", description = "Tutor não encontrado")
    public Tutor atualizar(@PathVariable Long id, @RequestBody @Valid TutorRequest request) {
        return tutorService.atualizar(id, Tutor.builder()
                .nome(request.nome())
                .email(request.email())
                .telefone(request.telefone())
                .cpf(request.cpf())
                .build());
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