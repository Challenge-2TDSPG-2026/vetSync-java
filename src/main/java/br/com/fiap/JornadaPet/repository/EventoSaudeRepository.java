package br.com.fiap.JornadaPet.repository;

import br.com.fiap.JornadaPet.entity.EventoSaude;
import br.com.fiap.JornadaPet.entity.EventoSaude.StatusEvento;
import br.com.fiap.JornadaPet.entity.EventoSaude.TipoEvento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoSaudeRepository extends JpaRepository<EventoSaude, Long> {
    Page<EventoSaude> findByPetId(Long petId, Pageable pageable);
    List<EventoSaude> findByPetIdAndStatus(Long petId, StatusEvento status);
    List<EventoSaude> findByPetIdAndTipo(Long petId, TipoEvento tipo);
}