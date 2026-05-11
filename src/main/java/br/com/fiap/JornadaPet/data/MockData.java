package br.com.fiap.JornadaPet.data;

import br.com.fiap.JornadaPet.entity.EventoSaude;
import br.com.fiap.JornadaPet.entity.EventoSaude.StatusEvento;
import br.com.fiap.JornadaPet.entity.EventoSaude.TipoEvento;
import br.com.fiap.JornadaPet.entity.Pet;
import br.com.fiap.JornadaPet.entity.Tutor;
import br.com.fiap.JornadaPet.service.EventoService;
import br.com.fiap.JornadaPet.service.PetService;
import br.com.fiap.JornadaPet.service.TutorService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class MockData {

    private final TutorService tutorService;
    private final PetService petService;
    private final EventoService eventoService;

    @PostConstruct
    public void init() {

        // --- Tutores ---
        Tutor maria = tutorService.cadastrar(Tutor.builder()
                .nome("Maria Silva")
                .email("maria@email.com")
                .telefone("11999990001")
                .cpf("111.111.111-11")
                .build());

        Tutor joao = tutorService.cadastrar(Tutor.builder()
                .nome("João Souza")
                .email("joao@email.com")
                .telefone("11999990002")
                .cpf("222.222.222-22")
                .build());


        Pet buddy = petService.cadastrar(Pet.builder()
                .nome("Buddy")
                .especie("cão")
                .raca("Golden Retriever")
                .peso(5.0)
                .dataNascimento(LocalDate.now().minusMonths(3))
                .sexo("M")
                .castrado(false)
                .observacoes("Filhote muito ativo")
                .build(), maria.getId());


        Pet luna = petService.cadastrar(Pet.builder()
                .nome("Luna")
                .especie("gato")
                .raca("Siamês")
                .peso(3.5)
                .dataNascimento(LocalDate.now().minusYears(4))
                .sexo("F")
                .castrado(true)
                .observacoes("Gata tranquila")
                .build(), maria.getId());


        Pet rex = petService.cadastrar(Pet.builder()
                .nome("Rex")
                .especie("cão")
                .raca("Pastor Alemão")
                .peso(28.0)
                .dataNascimento(LocalDate.now().minusYears(2))
                .sexo("M")
                .castrado(false)
                .observacoes("Muito esperto")
                .build(), joao.getId());




        eventoService.registrar(EventoSaude.builder()
                .tipo(TipoEvento.BANHO)
                .descricao("Banho e tosa atrasado")
                .dataProxima(LocalDate.now().minusDays(10)) // data passada = atrasado
                .build(), buddy.getId());


        eventoService.registrar(EventoSaude.builder()
                .tipo(TipoEvento.CONSULTA)
                .descricao("Consulta de rotina")
                .status(StatusEvento.REALIZADO)
                .dataRealizacao(LocalDate.now().minusMonths(1))
                .dataProxima(LocalDate.now().plusMonths(11))
                .build(), luna.getId());


        eventoService.registrar(EventoSaude.builder()
                .tipo(TipoEvento.VACINA)
                .descricao("Vacina antirrábica anual")
                .dataProxima(LocalDate.now().plusDays(15))
                .build(), rex.getId());

    }

}