package br.com.fiap.VetSync.controller;

import br.com.fiap.VetSync.entity.EventoSaude;
import br.com.fiap.VetSync.service.EventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/pets/{idPet}/eventos")
@RequiredArgsConstructor
@Tag(name = "Eventos de Saúde", description = "Histórico de eventos de saúde do pet")
public class EventoController {

    private final EventoService eventoService;

    public record EventoRequest(
            Long idTipoEvento,
            Long idVeterinario,
            LocalDate dtEvento,
            String dsObservacao,
            BigDecimal vlCusto
    ) {}

    public record EventoResponse(
            Long idEvento,
            String nmTipoEvento,
            String dsCategoria,
            String nmVeterinario,
            LocalDate dtEvento,
            String dsObservacao,
            BigDecimal vlCusto,
            Long idPet
    ) {}

    private EventoResponse toResponse(EventoSaude evento) {
        return new EventoResponse(
                evento.getIdEvento(),
                evento.getTipoEvento() != null ? evento.getTipoEvento().getNmTipoEvento() : null,
                evento.getTipoEvento() != null ? evento.getTipoEvento().getDsCategoria() : null,
                evento.getVeterinario() != null ? evento.getVeterinario().getNmVeterinario() : null,
                evento.getDtEvento(),
                evento.getDsObservacao(),
                evento.getVlCusto(),
                evento.getPet() != null ? evento.getPet().getIdPet() : null
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('VETERINARIO')")
    @Operation(summary = "Registrar evento de saúde. Somente VETERINARIO.")
    public EventoResponse registrar(@PathVariable Long idPet, @RequestBody EventoRequest request) {
        EventoSaude evento = EventoSaude.builder()
                .dtEvento(request.dtEvento())
                .dsObservacao(request.dsObservacao())
                .vlCusto(request.vlCusto() != null ? request.vlCusto() : BigDecimal.ZERO)
                .build();
        return toResponse(eventoService.registrar(evento, idPet, request.idTipoEvento(), request.idVeterinario()));
    }

    @GetMapping
    @Operation(summary = "Listar eventos do pet")
    public List<EventoResponse> listar(@PathVariable Long idPet) {
        return eventoService.listarPorPet(idPet).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/gasto-total")
    @Operation(summary = "Somar o gasto total em eventos de saúde do pet")
    public BigDecimal gastoTotal(@PathVariable Long idPet) {
        return eventoService.calcularGastoTotal(idPet);
    }

    @GetMapping("/alertas")
    @Operation(summary = "Histórico + alerta", description = "Para cada tipo de evento já registrado, mostra há quantos meses foi a última vez e se já passou de 12 meses (pode estar atrasado).")
    public List<EventoService.AlertaEvento> alertas(@PathVariable Long idPet) {
        return eventoService.gerarAlertas(idPet);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar evento por ID")
    public EventoResponse buscarPorId(@PathVariable Long idPet, @PathVariable Long id) {
        return toResponse(eventoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('VETERINARIO')")
    @Operation(summary = "Atualizar evento. Somente VETERINARIO.")
    public EventoResponse atualizar(@PathVariable Long idPet, @PathVariable Long id,
                                    @RequestBody EventoRequest request) {
        EventoSaude eventoAtualizado = EventoSaude.builder()
                .dtEvento(request.dtEvento())
                .dsObservacao(request.dsObservacao())
                .vlCusto(request.vlCusto())
                .build();
        return toResponse(eventoService.atualizar(id, eventoAtualizado));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('VETERINARIO')")
    @Operation(summary = "Deletar evento. Somente VETERINARIO.")
    public void deletar(@PathVariable Long idPet, @PathVariable Long id) {
        eventoService.deletar(id);
    }
}