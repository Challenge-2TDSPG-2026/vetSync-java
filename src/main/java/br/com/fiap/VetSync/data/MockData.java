package br.com.fiap.VetSync.data;

import br.com.fiap.VetSync.entity.*;
import br.com.fiap.VetSync.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Configuration
@ConditionalOnProperty(name = "app.mockdata.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class MockData {

    private final TutorRepository tutorRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final ClinicaRepository clinicaRepository;
    private final EspecieRepository especieRepository;
    private final RacaRepository racaRepository;
    private final PetRepository petRepository;
    private final TipoEventoRepository tipoEventoRepository;
    private final EventoSaudeRepository eventoSaudeRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        if (tutorRepository.count() > 0) {
            return;
        }


        Especie cachorro = especieRepository.save(Especie.builder().nmEspecie("Cachorro").build());
        Especie gato = especieRepository.save(Especie.builder().nmEspecie("Gato").build());

        Raca golden = racaRepository.save(Raca.builder().nmRaca("Golden Retriever").especie(cachorro).build());
        Raca siames = racaRepository.save(Raca.builder().nmRaca("Siamês").especie(gato).build());
        Raca pastorAlemao = racaRepository.save(Raca.builder().nmRaca("Pastor Alemão").especie(cachorro).build());


        Clinica clinica = clinicaRepository.save(Clinica.builder()
                .nmClinica("Clyvo Vet")
                .dsCnpj("12345678000199")
                .dsCidade("São Paulo")
                .dsUf("SP")
                .build());

        Veterinario dra = veterinarioRepository.save(Veterinario.builder()
                .nmVeterinario("Dra. Ana Costa")
                .nrCrmv("SP-12345")
                .dsEmail("ana.vet@clyvovet.com")
                .dsSenha(passwordEncoder.encode("senha123"))
                .clinica(clinica)
                .build());


        TipoEvento vacina = tipoEventoRepository.save(TipoEvento.builder()
                .nmTipoEvento("Vacina").dsCategoria("PREVENTIVO").nrPontos(20).build());
        TipoEvento consulta = tipoEventoRepository.save(TipoEvento.builder()
                .nmTipoEvento("Consulta de rotina").dsCategoria("PREVENTIVO").nrPontos(15).build());
        TipoEvento banho = tipoEventoRepository.save(TipoEvento.builder()
                .nmTipoEvento("Banho e tosa").dsCategoria("BEM_ESTAR").nrPontos(5).build());


        Tutor maria = tutorRepository.save(Tutor.builder()
                .nmTutor("Maria Silva")
                .dsEmail("maria@email.com")
                .dsSenha(passwordEncoder.encode("senha123"))
                .nrTelefone("11999990001")
                .dsCpf("11111111111")
                .build());

        Tutor joao = tutorRepository.save(Tutor.builder()
                .nmTutor("João Souza")
                .dsEmail("joao@email.com")
                .dsSenha(passwordEncoder.encode("senha123"))
                .nrTelefone("11999990002")
                .dsCpf("22222222222")
                .build());


        Pet buddy = petRepository.save(Pet.builder()
                .nmPet("Buddy")
                .dtNascimento(LocalDate.now().minusMonths(3))
                .dsSexo("M")
                .nrPesoKg(new BigDecimal("5.0"))
                .tutor(maria)
                .raca(golden)
                .build());

        Pet luna = petRepository.save(Pet.builder()
                .nmPet("Luna")
                .dtNascimento(LocalDate.now().minusYears(4))
                .dsSexo("F")
                .nrPesoKg(new BigDecimal("3.5"))
                .tutor(maria)
                .raca(siames)
                .build());

        Pet rex = petRepository.save(Pet.builder()
                .nmPet("Rex")
                .dtNascimento(LocalDate.now().minusYears(2))
                .dsSexo("M")
                .nrPesoKg(new BigDecimal("28.0"))
                .tutor(joao)
                .raca(pastorAlemao)
                .build());


        eventoSaudeRepository.save(EventoSaude.builder()
                .pet(buddy).tipoEvento(banho).veterinario(dra)
                .dtEvento(LocalDate.now().minusDays(10))
                .dsObservacao("Banho e tosa de rotina")
                .vlCusto(new BigDecimal("80.00"))
                .dsStatus(StatusEvento.CONCLUIDO)
                .build());

        eventoSaudeRepository.save(EventoSaude.builder()
                .pet(luna).tipoEvento(consulta).veterinario(dra)
                .dtEvento(LocalDate.now().minusMonths(1))
                .dsObservacao("Consulta de rotina, tudo normal")
                .vlCusto(new BigDecimal("150.00"))
                .dsStatus(StatusEvento.CONCLUIDO)
                .build());

        eventoSaudeRepository.save(EventoSaude.builder()
                .pet(rex).tipoEvento(vacina).veterinario(dra)
                .dtEvento(LocalDate.now().minusDays(5))
                .dsObservacao("Vacina antirrábica anual")
                .vlCusto(new BigDecimal("120.00"))
                .dsStatus(StatusEvento.CONCLUIDO)
                .build());
    }
}