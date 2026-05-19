package br.com.fiap.JornadaPet.repository;

import br.com.fiap.JornadaPet.entity.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TutorRepository extends JpaRepository<Tutor, Long> {
    Optional<Tutor> findByEmail(String email);
    boolean existsByEmail(String email);
}