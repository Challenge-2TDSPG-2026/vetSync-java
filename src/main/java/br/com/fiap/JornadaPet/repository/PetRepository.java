package br.com.fiap.JornadaPet.repository;
import br.com.fiap.JornadaPet.entity.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {
    Page<Pet> findByTutorId(Long tutorId, Pageable pageable);
    List<Pet> findByTutorId(Long tutorId);
    List<Pet> findByEspecie(String especie);
    List<Pet> findByRaca(String raca);
    List<Pet> findByTutorIdAndEspecie(Long tutorId, String especie);
}