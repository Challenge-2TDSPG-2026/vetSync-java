package br.com.fiap.VetSync.service;

import br.com.fiap.VetSync.entity.EventoSaude;
import br.com.fiap.VetSync.entity.Pet;
import br.com.fiap.VetSync.entity.TipoEvento;
import br.com.fiap.VetSync.entity.Veterinario;
import br.com.fiap.VetSync.repository.EventoSaudeRepository;
import br.com.fiap.VetSync.repository.TipoEventoRepository;
import br.com.fiap.VetSync.repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoSaudeRepository eventoSaudeRepository;
    private final PetService petService;
    private final TipoEventoRepository tipoEventoRepository;
    private final VeterinarioRepository veterinarioRepository;

    // Regra de negocio: se o ultimo evento de um tipo foi ha 12 meses
    // ou mais, o pet "pode estar atrasado" naquele tipo de cuidado.
    private static final long MESES_LIMITE_ATRASO = 12;

    public record AlertaEvento(
            String nmTipoEvento,
            LocalDate ultimaData,
            long mesesDesdeUltimo,
            boolean atrasado,
            String mensagem
    ) {}

    public EventoSaude registrar(EventoSaude evento, Long idPet, Long idTipoEvento, Long idVeterinario) {
        Pet pet = petService.buscarPorId(idPet);
        TipoEvento tipoEvento = tipoEventoRepository.findById(idTipoEvento).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de evento não encontrado: " + idTipoEvento)
        );
        evento.setPet(pet);
        evento.setTipoEvento(tipoEvento);
        if (idVeterinario != null) {
            Veterinario vet = veterinarioRepository.findById(idVeterinario).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinário não encontrado: " + idVeterinario)
            );
            evento.setVeterinario(vet);
        }
        return eventoSaudeRepository.save(evento);
    }

    public EventoSaude buscarPorId(Long id) {
        return eventoSaudeRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado com id: " + id)
        );
    }

    public List<EventoSaude> listarPorPet(Long idPet) {
        return eventoSaudeRepository.findByPet_IdPet(idPet);
    }

    public BigDecimal calcularGastoTotal(Long idPet) {
        return listarPorPet(idPet).stream()
                .map(EventoSaude::getVlCusto)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public List<AlertaEvento> gerarAlertas(Long idPet) {
        List<EventoSaude> eventos = listarPorPet(idPet);

        Map<TipoEvento, EventoSaude> maisRecentePorTipo = eventos.stream()
                .collect(Collectors.toMap(
                        EventoSaude::getTipoEvento,
                        e -> e,
                        (e1, e2) -> e1.getDtEvento().isAfter(e2.getDtEvento()) ? e1 : e2
                ));

        List<AlertaEvento> alertas = new ArrayList<>();
        for (Map.Entry<TipoEvento, EventoSaude> entry : maisRecentePorTipo.entrySet()) {
            TipoEvento tipo = entry.getKey();
            EventoSaude ultimo = entry.getValue();
            long meses = ChronoUnit.MONTHS.between(ultimo.getDtEvento(), LocalDate.now());
            boolean atrasado = meses >= MESES_LIMITE_ATRASO;
            String mensagem = atrasado
                    ? "Último \"" + tipo.getNmTipoEvento() + "\" foi há " + meses + " meses — pode estar atrasado"
                    : "Último \"" + tipo.getNmTipoEvento() + "\" foi há " + meses + " meses — em dia";
            alertas.add(new AlertaEvento(tipo.getNmTipoEvento(), ultimo.getDtEvento(), meses, atrasado, mensagem));
        }

        alertas.sort(Comparator.comparing(AlertaEvento::atrasado).reversed());
        return alertas;
    }

    public EventoSaude atualizar(Long id, EventoSaude eventoAtualizado) {
        EventoSaude evento = buscarPorId(id);
        evento.setDtEvento(eventoAtualizado.getDtEvento());
        evento.setDsObservacao(eventoAtualizado.getDsObservacao());
        evento.setVlCusto(eventoAtualizado.getVlCusto());
        return eventoSaudeRepository.save(evento);
    }

    public void deletar(Long id) {
        EventoSaude evento = buscarPorId(id);
        eventoSaudeRepository.delete(evento);
    }
}