package br.com.fiap.VetSync.controller;

import br.com.fiap.VetSync.entity.EventoSaude;
import br.com.fiap.VetSync.entity.StatusEvento;
import br.com.fiap.VetSync.service.EventoService;
import br.com.fiap.VetSync.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/eventos")
@RequiredArgsConstructor
@Tag(name = "Eventos de Saúde", description = "Fluxo solicitado → confirmado → concluído/cancelado entre tutor e veterinário")
public class EventoController {

    private final EventoService eventoService;
    private final PetService petService;

    public record EventoSolicitarRequest(
            Long idPet, Long idTipoEvento, Long idVeterinario, LocalDate dtEvento, String dsObservacao
    ) {}

    public record EventoConcluirRequest(String dsObservacao, BigDecimal vlCusto) {}

    public record EventoCancelarRequest(String motivo) {}

    public record EventoResponse(
            Long idEvento,
            String status,
            String nmTipoEvento,
            String dsCategoria,
            String nmVeterinario,
            LocalDate dtEvento,
            String dsObservacao,
            String motivoCancelamento,
            BigDecimal vlCusto,
            Long idPet
    ) {}

    private EventoResponse toResponse(EventoSaude evento) {
        return new EventoResponse(
                evento.getIdEvento(),
                evento.getDsStatus().name(),
                evento.getTipoEvento() != null ? evento.getTipoEvento().getNmTipoEvento() : null,
                evento.getTipoEvento() != null ? evento.getTipoEvento().getDsCategoria() : null,
                evento.getVeterinario() != null ? evento.getVeterinario().getNmVeterinario() : null,
                evento.getDtEvento(),
                evento.getDsObservacao(),
                evento.getDsMotivoCancelamento(),
                evento.getVlCusto(),
                evento.getPet() != null ? evento.getPet().getIdPet() : null
        );
    }

    private boolean ehVeterinario(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_VETERINARIO"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('TUTOR')")
    @Operation(summary = "Tutor solicita um evento de saúde para um pet dele, escolhendo o veterinário")
    public EventoResponse solicitar(Authentication authentication, @RequestBody EventoSolicitarRequest request) {
        boolean donoDoPet = petService.buscarPorId(request.idPet()).getTutor().getDsEmail()
                .equalsIgnoreCase(authentication.getName());
        if (!donoDoPet) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esse pet não pertence a você");
        }
        EventoSaude evento = EventoSaude.builder()
                .dtEvento(request.dtEvento())
                .dsObservacao(request.dsObservacao())
                .build();
        return toResponse(eventoService.solicitar(evento, request.idPet(), request.idTipoEvento(), request.idVeterinario()));
    }

    @GetMapping
    @Operation(summary = "Listar eventos", description = "Tutor vê os eventos dos próprios pets; veterinário vê os eventos atribuídos a ele.")
    public List<EventoResponse> listar(Authentication authentication) {
        List<EventoSaude> eventos = ehVeterinario(authentication)
                ? eventoService.listarParaVeterinario(authentication.getName())
                : eventoService.listarParaTutor(authentication.getName());
        return eventos.stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@eventoSecurity.isRelacionado(#id, authentication)")
    @Operation(summary = "Buscar evento por ID (só tutor dono ou veterinário responsável)")
    public EventoResponse buscarPorId(@PathVariable Long id) {
        return toResponse(eventoService.buscarPorId(id));
    }

    @PatchMapping("/{id}/confirmar")
    @PreAuthorize("hasRole('VETERINARIO') and @eventoSecurity.isVeterinarioResponsavel(#id, authentication)")
    @Operation(summary = "Veterinário confirma um evento SOLICITADO")
    public EventoResponse confirmar(@PathVariable Long id) {
        return toResponse(eventoService.confirmar(id));
    }

    @PatchMapping("/{id}/concluir")
    @PreAuthorize("hasRole('VETERINARIO') and @eventoSecurity.isVeterinarioResponsavel(#id, authentication)")
    @Operation(summary = "Veterinário conclui um evento CONFIRMADO, com observações clínicas e custo final")
    public EventoResponse concluir(@PathVariable Long id, @RequestBody EventoConcluirRequest request) {
        return toResponse(eventoService.concluir(id, request.dsObservacao(), request.vlCusto()));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("@eventoSecurity.isRelacionado(#id, authentication)")
    @Operation(summary = "Cancelar evento (tutor ou veterinário envolvido), com motivo")
    public EventoResponse cancelar(@PathVariable Long id, @RequestBody EventoCancelarRequest request) {
        return toResponse(eventoService.cancelar(id, request.motivo()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@eventoSecurity.canDelete(#id, authentication)")
    @Operation(summary = "Remover evento", description = "Veterinário sempre pode; tutor só enquanto o evento ainda está SOLICITADO.")
    public void deletar(@PathVariable Long id) {
        eventoService.deletar(id);
    }

    @GetMapping("/pet/{idPet}/gasto-total")
    @Operation(summary = "Somar o gasto total (só eventos CONCLUIDOS) de um pet")
    public BigDecimal gastoTotal(@PathVariable Long idPet) {
        return eventoService.calcularGastoTotal(idPet);
    }

    @GetMapping("/pet/{idPet}/alertas")
    @Operation(summary = "Histórico + alerta de atraso por tipo de evento (só considera eventos CONCLUIDOS)")
    public List<EventoService.AlertaEvento> alertas(@PathVariable Long idPet) {
        return eventoService.gerarAlertas(idPet);
    }
}