package br.com.fiap.VetSync.repository;

import br.com.fiap.VetSync.entity.Prescricao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescricaoRepository extends JpaRepository<Prescricao, Long> {
    List<Prescricao> findByEvento_IdEvento(Long idEvento);
}