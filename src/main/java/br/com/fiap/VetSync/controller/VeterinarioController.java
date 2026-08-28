package br.com.fiap.VetSync.controller;

import br.com.fiap.VetSync.entity.BloqueioAgenda;
import br.com.fiap.VetSync.entity.Disponibilidade;
import br.com.fiap.VetSync.entity.Veterinario;
import br.com.fiap.VetSync.service.AgendaService;
import br.com.fiap.VetSync.service.JwtService;
import br.com.fiap.VetSync.service.VeterinarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/veterinarios")
@RequiredArgsConstructor
@Tag(name = "Veterinários", description = "Cadastro, perfil e agenda (disponibilidade e bloqueios)")
public class VeterinarioController {

    private final VeterinarioService veterinarioService;
    private final AgendaService agendaService;
    private final JwtService jwtService;

    private static final int SENHA_MIN_LENGTH = 6;



    public record VeterinarioRequest(String nome, String crmv, String email, String senha, Long idClinica) {}
    public record VeterinarioAtualizarRequest(String nome, Long idClinica) {}

    public record VeterinarioResponse(Long idVeterinario, String nmVeterinario, String nrCrmv, String dsEmail, Long idClinica, String nmClinica) {}
    public record CadastroResponse(String token, Long idVeterinario, String email, String nome) {}

    private VeterinarioResponse toResponse(Veterinario vet) {
        return new VeterinarioResponse(
                vet.getIdVeterinario(), vet.getNmVeterinario(), vet.getNrCrmv(), vet.getDsEmail(),
                vet.getClinica() != null ? vet.getClinica().getIdClinica() : null,
                vet.getClinica() != null ? vet.getClinica().getNmClinica() : null
        );
    }

    @GetMapping
    @Operation(summary = "Listar veterinários (para o tutor escolher um)")
    public List<VeterinarioResponse> listar() {
        return veterinarioService.listarTodos().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar veterinário por ID")
    public VeterinarioResponse buscarPorId(@PathVariable Long id) {
        return toResponse(veterinarioService.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar veterinário", description = "Requer idClinica de uma clínica já existente. Senha com mínimo 6 caracteres.")
    public CadastroResponse cadastrar(@RequestBody VeterinarioRequest request) {
        if (request.senha() == null || request.senha().length() < SENHA_MIN_LENGTH) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A senha deve ter pelo menos " + SENHA_MIN_LENGTH + " caracteres");
        }
        Veterinario vet = veterinarioService.cadastrar(request.nome(), request.crmv(), request.email(), request.senha(), request.idClinica());
        return new CadastroResponse(jwtService.gerarToken(vet.getDsEmail()), vet.getIdVeterinario(), vet.getDsEmail(), vet.getNmVeterinario());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@veterinarioSecurity.isSelf(#id, authentication)")
    @Operation(summary = "Atualizar dados do veterinário. Só o próprio.")
    public VeterinarioResponse atualizar(@PathVariable Long id, @RequestBody VeterinarioAtualizarRequest request) {
        return toResponse(veterinarioService.atualizar(id, request.nome(), request.idClinica()));
    }

    // ===== Disponibilidade =====

    public record DisponibilidadeRequest(Integer nrDiaSemana, String hrInicio, String hrFim) {}
    public record DisponibilidadeResponse(Long idDisponibilidade, Integer nrDiaSemana, String hrInicio, String hrFim) {}

    private DisponibilidadeResponse toResponse(Disponibilidade d) {
        return new DisponibilidadeResponse(d.getIdDisponibilidade(), d.getNrDiaSemana(), d.getHrInicio(), d.getHrFim());
    }

    @GetMapping("/{id}/disponibilidade")
    @Operation(summary = "Listar horários fixos de atendimento do veterinário")
    public List<DisponibilidadeResponse> listarDisponibilidade(@PathVariable Long id) {
        return agendaService.listarDisponibilidade(id).stream().map(this::toResponse).toList();
    }

    @PostMapping("/{id}/disponibilidade")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@veterinarioSecurity.isSelf(#id, authentication)")
    @Operation(summary = "Adicionar horário fixo de atendimento. nrDiaSemana: 1=segunda...7=domingo. Horários no formato HH:mm.")
    public DisponibilidadeResponse adicionarDisponibilidade(@PathVariable Long id, @RequestBody DisponibilidadeRequest request) {
        return toResponse(agendaService.adicionarDisponibilidade(id, request.nrDiaSemana(), request.hrInicio(), request.hrFim()));
    }

    @DeleteMapping("/{id}/disponibilidade/{idDisponibilidade}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@veterinarioSecurity.isSelf(#id, authentication)")
    @Operation(summary = "Remover horário fixo de atendimento")
    public void removerDisponibilidade(@PathVariable Long id, @PathVariable Long idDisponibilidade) {
        agendaService.removerDisponibilidade(id, idDisponibilidade);
    }

    // ===== Bloqueios =====

    public record BloqueioRequest(LocalDate dtInicio, LocalDate dtFim, String motivo) {}
    public record BloqueioResponse(Long idBloqueio, LocalDate dtInicio, LocalDate dtFim, String motivo) {}

    private BloqueioResponse toResponse(BloqueioAgenda b) {
        return new BloqueioResponse(b.getIdBloqueio(), b.getDtInicio(), b.getDtFim(), b.getDsMotivo());
    }

    @GetMapping("/{id}/bloqueios")
    @Operation(summary = "Listar bloqueios de agenda (férias, compromissos, etc.)")
    public List<BloqueioResponse> listarBloqueios(@PathVariable Long id) {
        return agendaService.listarBloqueios(id).stream().map(this::toResponse).toList();
    }

    @PostMapping("/{id}/bloqueios")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@veterinarioSecurity.isSelf(#id, authentication)")
    @Operation(summary = "Adicionar bloqueio de agenda (um dia ou um período)")
    public BloqueioResponse adicionarBloqueio(@PathVariable Long id, @RequestBody BloqueioRequest request) {
        return toResponse(agendaService.adicionarBloqueio(id, request.dtInicio(), request.dtFim(), request.motivo()));
    }

    @DeleteMapping("/{id}/bloqueios/{idBloqueio}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@veterinarioSecurity.isSelf(#id, authentication)")
    @Operation(summary = "Remover bloqueio de agenda")
    public void removerBloqueio(@PathVariable Long id, @PathVariable Long idBloqueio) {
        agendaService.removerBloqueio(id, idBloqueio);
    }
}