package br.com.fiap.JornadaPet.service;

import br.com.fiap.JornadaPet.entity.EventoSaude;
import br.com.fiap.JornadaPet.entity.EventoSaude.StatusEvento;
import br.com.fiap.JornadaPet.entity.Pet;
import br.com.fiap.JornadaPet.repository.EventoSaudeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoSaudeRepository eventoSaudeRepository;
    private final PetService petService;


    public EventoSaude registrar(EventoSaude evento, Long petId) {
        Pet pet = petService.buscarPorId(petId);
        evento.setPet(pet);
        return eventoSaudeRepository.save(evento);
    }


    public EventoSaude buscarPorId(Long id) {
        return eventoSaudeRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Evento não encontrado com id: " + id)
        );
    }


    public Page<EventoSaude> listarPorPet(Long petId, Pageable pageable) {
        return eventoSaudeRepository.findByPetId(petId, pageable);
    }


    public List<EventoSaude> listarPendentes(Long petId) {
        List<EventoSaude> pendentes = eventoSaudeRepository
                .findByPetIdAndStatus(petId, StatusEvento.PENDENTE);


        pendentes.forEach(e -> {
            if (e.getDataProxima() != null && e.getDataProxima().isBefore(LocalDate.now())) {
                e.setStatus(StatusEvento.ATRASADO);
                eventoSaudeRepository.save(e);
            }
        });

        return eventoSaudeRepository.findByPetIdAndStatus(petId, StatusEvento.PENDENTE);
    }


    public List<EventoSaude> listarAtrasados(Long petId) {
        listarPendentes(petId); // força atualização de status
        return eventoSaudeRepository.findByPetIdAndStatus(petId, StatusEvento.ATRASADO);
    }


    public EventoSaude marcarRealizado(Long id, LocalDate dataProxima) {
        EventoSaude evento = buscarPorId(id);
        evento.setStatus(StatusEvento.REALIZADO);
        evento.setDataRealizacao(LocalDate.now());
        evento.setDataProxima(dataProxima);
        return eventoSaudeRepository.save(evento);
    }


    public EventoSaude atualizar(Long id, EventoSaude eventoAtualizado) {
        EventoSaude evento = buscarPorId(id);
        evento.setTipo(eventoAtualizado.getTipo());
        evento.setStatus(eventoAtualizado.getStatus());
        evento.setDescricao(eventoAtualizado.getDescricao());
        evento.setDataProxima(eventoAtualizado.getDataProxima());
        evento.setDataRealizacao(eventoAtualizado.getDataRealizacao());
        return eventoSaudeRepository.save(evento);
    }


    public void deletar(Long id) {
        EventoSaude evento = buscarPorId(id);
        eventoSaudeRepository.delete(evento);
    }

}