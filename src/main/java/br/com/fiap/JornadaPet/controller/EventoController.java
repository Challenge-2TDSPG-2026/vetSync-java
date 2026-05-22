package br.com.fiap.JornadaPet.controller;

import br.com.fiap.JornadaPet.entity.EventoSaude;
import br.com.fiap.JornadaPet.entity.EventoSaude.StatusEvento;
import br.com.fiap.JornadaPet.entity.EventoSaude.TipoEvento;
import br.com.fiap.JornadaPet.service.EventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/pets/{petId}/eventos")
@RequiredArgsConstructor
@Tag(name = "Eventos de Saúde", description = "Jornada contínua de saúde do pet — vacinas, vermifugações, check-ups e mais")
public class EventoController {

    private final EventoService eventoService;

   
    public record EventoRequest(
            TipoEvento tipo,
            String descricao,
            LocalDate dataRealizacao,
            LocalDate dataProxima
    ) {}

    public record RealizadoRequest(LocalDate dataProxima) {}

    
    public record EventoResponse(
            Long id,
            TipoEvento tipo,
            StatusEvento status,
            String descricao,
            LocalDate dataRealizacao,
            LocalDate dataProxima,
            Long petId
    ) {}

    private EventoResponse toResponse(EventoSaude evento) {
        return new EventoResponse(
                evento.getId(),
                evento.getTipo(),
                evento.getStatus(),
                evento.getDescricao(),
                evento.getDataRealizacao(),
                evento.getDataProxima(),
                evento.getPet() != null ? evento.getPet().getId() : null
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar evento de saúde", description = "Registra vacina, vermifugo, banho, check-up, etc.")
    @ApiResponse(responseCode = "201", description = "Evento registrado")
    @ApiResponse(responseCode = "404", description = "Pet não encontrado")
    public EventoResponse registrar(@PathVariable Long petId,
                                    @RequestBody EventoRequest request) {
        EventoSaude evento = EventoSaude.builder()
                .tipo(request.tipo())
                .descricao(request.descricao())
                .dataRealizacao(request.dataRealizacao())
                .dataProxima(request.dataProxima())
                .build();
        return toResponse(eventoService.registrar(evento, petId));
    }

    @GetMapping
    @Operation(summary = "Listar eventos do pet", description = "Suporta paginação e ordenação por data")
    public Page<EventoResponse> listar(
            @PathVariable Long petId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dataProxima") String sort) {
        return eventoService.listarPorPet(petId,
                PageRequest.of(page, size, Sort.by(sort)))
                .map(this::toResponse);
    }

    @GetMapping("/pendentes")
    @Operation(summary = "Listar eventos pendentes", description = "Retorna eventos pendentes. Status é atualizado para ATRASADO automaticamente se a data passou.")
    public List<EventoResponse> listarPendentes(@PathVariable Long petId) {
        return eventoService.listarPendentes(petId).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/atrasados")
    @Operation(summary = "Listar eventos atrasados", description = "Retorna eventos com dataProxima no passado e ainda não realizados")
    public List<EventoResponse> listarAtrasados(@PathVariable Long petId) {
        return eventoService.listarAtrasados(petId).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar evento por ID")
    @ApiResponse(responseCode = "200", description = "Evento encontrado")
    @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    public EventoResponse buscarPorId(@PathVariable Long petId, @PathVariable Long id) {
        return toResponse(eventoService.buscarPorId(id));
    }

    @PatchMapping("/{id}/realizado")
    @Operation(summary = "Marcar evento como realizado", description = "Define status como REALIZADO e registra dataRealizacao = hoje")
    @ApiResponse(responseCode = "200", description = "Evento marcado como realizado")
    public EventoResponse marcarRealizado(@PathVariable Long petId,
                                          @PathVariable Long id,
                                          @RequestBody RealizadoRequest request) {
        return toResponse(eventoService.marcarRealizado(id, request.dataProxima()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar evento")
    public EventoResponse atualizar(@PathVariable Long petId,
                                    @PathVariable Long id,
                                    @RequestBody EventoRequest request) {
        EventoSaude eventoAtualizado = EventoSaude.builder()
                .tipo(request.tipo())
                .descricao(request.descricao())
                .dataRealizacao(request.dataRealizacao())
                .dataProxima(request.dataProxima())
                .build();
        return toResponse(eventoService.atualizar(id, eventoAtualizado));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar evento")
    public void deletar(@PathVariable Long petId, @PathVariable Long id) {
        eventoService.deletar(id);
    }

}
