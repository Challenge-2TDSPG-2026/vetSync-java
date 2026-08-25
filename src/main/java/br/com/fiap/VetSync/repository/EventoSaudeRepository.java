package br.com.fiap.VetSync.repository;

import br.com.fiap.VetSync.entity.EventoSaude;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoSaudeRepository extends JpaRepository<EventoSaude, Long> {
    List<EventoSaude> findByPet_IdPet(Long idPet);
}