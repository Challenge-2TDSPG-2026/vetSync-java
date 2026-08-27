package br.com.fiap.VetSync.service;

import br.com.fiap.VetSync.entity.*;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoSaudeRepository eventoSaudeRepository;
    private final PetService petService;
    private final TipoEventoRepository tipoEventoRepository;
    private final VeterinarioRepository veterinarioRepository;

    private static final long MESES_LIMITE_ATRASO = 12;

    public record AlertaEvento(
            String nmTipoEvento, LocalDate ultimaData, long mesesDesdeUltimo, boolean atrasado, String mensagem
    ) {}


    public EventoSaude solicitar(EventoSaude evento, Long idPet, Long idTipoEvento, Long idVeterinario) {
        Pet pet = petService.buscarPorId(idPet);
        TipoEvento tipoEvento = tipoEventoRepository.findById(idTipoEvento).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de evento não encontrado: " + idTipoEvento));
        Veterinario vet = veterinarioRepository.findById(idVeterinario).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veterinário não encontrado: " + idVeterinario));

        evento.setPet(pet);
        evento.setTipoEvento(tipoEvento);
        evento.setVeterinario(vet);
        evento.setDsStatus(StatusEvento.SOLICITADO);
        return eventoSaudeRepository.save(evento);
    }

    public EventoSaude buscarPorId(Long id) {
        return eventoSaudeRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado com id: " + id));
    }

    public List<EventoSaude> listarPorPet(Long idPet) {
        return eventoSaudeRepository.findByPet_IdPet(idPet);
    }

    public List<EventoSaude> listarParaTutor(String email) {
        return eventoSaudeRepository.findByPet_Tutor_DsEmailOrderByDtEventoDesc(email);
    }

    public List<EventoSaude> listarParaVeterinario(String email) {
        return eventoSaudeRepository.findByVeterinario_DsEmailOrderByDtEventoDesc(email);
    }


    public EventoSaude confirmar(Long id) {
        EventoSaude evento = buscarPorId(id);
        exigirStatus(evento, StatusEvento.SOLICITADO, "confirmar");
        evento.setDsStatus(StatusEvento.CONFIRMADO);
        return eventoSaudeRepository.save(evento);
    }

    public EventoSaude concluir(Long id, String dsObservacao, BigDecimal vlCusto) {
        EventoSaude evento = buscarPorId(id);
        exigirStatus(evento, StatusEvento.CONFIRMADO, "concluir");
        evento.setDsStatus(StatusEvento.CONCLUIDO);
        if (dsObservacao != null) {
            evento.setDsObservacao(dsObservacao);
        }
        evento.setVlCusto(vlCusto != null ? vlCusto : BigDecimal.ZERO);
        return eventoSaudeRepository.save(evento);

    }

    public EventoSaude cancelar(Long id, String motivo) {
        EventoSaude evento = buscarPorId(id);
        if (evento.getDsStatus() == StatusEvento.CONCLUIDO || evento.getDsStatus() == StatusEvento.CANCELADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível cancelar um evento que já está " + evento.getDsStatus());
        }
        evento.setDsStatus(StatusEvento.CANCELADO);
        evento.setDsMotivoCancelamento(motivo);
        return eventoSaudeRepository.save(evento);
    }

    private void exigirStatus(EventoSaude evento, StatusEvento esperado, String acao) {
        if (evento.getDsStatus() != esperado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Só é possível " + acao + " um evento que está " + esperado
                            + " (status atual: " + evento.getDsStatus() + ")");
        }
    }

    public void deletar(Long id) {
        EventoSaude evento = buscarPorId(id);
        eventoSaudeRepository.delete(evento);
    }


    public BigDecimal calcularGastoTotal(Long idPet) {
        return listarPorPet(idPet).stream()
                .filter(e -> e.getDsStatus() == StatusEvento.CONCLUIDO)
                .map(EventoSaude::getVlCusto)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<AlertaEvento> gerarAlertas(Long idPet) {
        List<EventoSaude> concluidos = listarPorPet(idPet).stream()
                .filter(e -> e.getDsStatus() == StatusEvento.CONCLUIDO)
                .toList();

        Map<TipoEvento, EventoSaude> maisRecentePorTipo = concluidos.stream()
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
}