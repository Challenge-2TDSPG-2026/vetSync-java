package br.com.fiap.VetSync.service;

import br.com.fiap.VetSync.entity.*;
import br.com.fiap.VetSync.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

    @Mock
    private EventoSaudeRepository eventoSaudeRepository;

    @Mock
    private PetService petService;

    @Mock
    private TipoEventoRepository tipoEventoRepository;

    @Mock
    private VeterinarioRepository veterinarioRepository;

    @Mock
    private PontosService pontosService;

    @Mock
    private PlanoItemRepository planoItemRepository;

    @Mock
    private PlanoTratamentoRepository planoTratamentoRepository;

    @InjectMocks
    private EventoService eventoService;

    @Test
    @DisplayName("Deve agendar evento de saúde com sucesso")
    void agendar_Sucesso() {
        Pet pet = Pet.builder().idPet(1L).build();
        TipoEvento tipo = TipoEvento.builder().idTipoEvento(2L).nmTipoEvento("Vacina").build();
        Veterinario vet = Veterinario.builder().idVeterinario(3L).build();

        when(petService.buscarPorId(1L)).thenReturn(pet);
        when(tipoEventoRepository.findById(2L)).thenReturn(Optional.of(tipo));
        when(veterinarioRepository.findById(3L)).thenReturn(Optional.of(vet));
        when(eventoSaudeRepository.save(any(EventoSaude.class))).thenAnswer(inv -> inv.getArgument(0));

        EventoSaude evento = EventoSaude.builder().dtEvento(LocalDate.now()).build();
        EventoSaude agendado = eventoService.agendar(evento, 1L, 2L, 3L);

        assertThat(agendado.getDsStatus()).isEqualTo(StatusEvento.AGENDADO);
        assertThat(agendado.getPet()).isEqualTo(pet);
        assertThat(agendado.getTipoEvento()).isEqualTo(tipo);
        assertThat(agendado.getVeterinario()).isEqualTo(vet);
    }

    @Test
    @DisplayName("Deve concluir evento com sucesso e lançar pontos")
    void concluir_Sucesso() {
        EventoSaude evento = EventoSaude.builder()
                .idEvento(10L)
                .dsStatus(StatusEvento.AGENDADO)
                .build();

        when(eventoSaudeRepository.findById(10L)).thenReturn(Optional.of(evento));
        when(eventoSaudeRepository.save(any(EventoSaude.class))).thenAnswer(inv -> inv.getArgument(0));
        when(planoItemRepository.findByEvento_IdEvento(10L)).thenReturn(Optional.empty());

        EventoSaude concluido = eventoService.concluir(10L, "Consulta ótima", new BigDecimal("150.00"));

        assertThat(concluido.getDsStatus()).isEqualTo(StatusEvento.CONCLUIDO);
        assertThat(concluido.getDsObservacao()).isEqualTo("Consulta ótima");
        assertThat(concluido.getVlCusto()).isEqualByComparingTo("150.00");
        verify(pontosService).lancarPendente(concluido);
    }

    @Test
    @DisplayName("Deve falhar ao tentar concluir evento que não está AGENDADO")
    void concluir_StatusInvalido() {
        EventoSaude evento = EventoSaude.builder()
                .idEvento(10L)
                .dsStatus(StatusEvento.CONCLUIDO)
                .build();

        when(eventoSaudeRepository.findById(10L)).thenReturn(Optional.of(evento));

        assertThatThrownBy(() -> eventoService.concluir(10L, "obs", BigDecimal.TEN))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Só é possível concluir um evento que está AGENDADO");

        verify(pontosService, never()).lancarPendente(any());
    }

    @Test
    @DisplayName("Ao concluir último item de plano sequencial, deve concluir o plano e creditar bônus")
    void concluir_ProcessarPlano_SucessoUltimoItem() {
        EventoSaude evento = EventoSaude.builder()
                .idEvento(10L)
                .dsStatus(StatusEvento.AGENDADO)
                .build();

        PlanoTratamento plano = PlanoTratamento.builder()
                .idPlano(100L)
                .dsStatus(StatusPlanoTratamento.EM_ANDAMENTO)
                .nrPontosBonus(50)
                .build();

        PlanoItem item1 = PlanoItem.builder().idItem(1L).plano(plano).nrOrdem(1).dsStatus(StatusPlanoItem.CONCLUIDO).build();
        PlanoItem item2 = PlanoItem.builder().idItem(2L).plano(plano).nrOrdem(2).dsStatus(StatusPlanoItem.AGENDADO).evento(evento).build();

        when(eventoSaudeRepository.findById(10L)).thenReturn(Optional.of(evento));
        when(eventoSaudeRepository.save(any(EventoSaude.class))).thenAnswer(inv -> inv.getArgument(0));
        when(planoItemRepository.findByEvento_IdEvento(10L)).thenReturn(Optional.of(item2));
        when(planoItemRepository.findByPlano_IdPlanoOrderByNrOrdemAsc(100L)).thenReturn(List.of(item1, item2));

        eventoService.concluir(10L, "Obs", BigDecimal.ZERO);

        assertThat(item2.getDsStatus()).isEqualTo(StatusPlanoItem.CONCLUIDO);
        assertThat(plano.getDsStatus()).isEqualTo(StatusPlanoTratamento.CONCLUIDO);
        verify(planoTratamentoRepository).save(plano);
        verify(pontosService).lancarBonusPendente(plano);
    }

    @Test
    @DisplayName("Ao concluir item fora de ordem no plano, deve marcar o plano como QUEBRADO")
    void concluir_ProcessarPlano_ForaDeOrdem() {
        EventoSaude evento = EventoSaude.builder()
                .idEvento(10L)
                .dsStatus(StatusEvento.AGENDADO)
                .build();

        PlanoTratamento plano = PlanoTratamento.builder()
                .idPlano(100L)
                .dsStatus(StatusPlanoTratamento.EM_ANDAMENTO)
                .build();

        PlanoItem item1 = PlanoItem.builder().idItem(1L).plano(plano).nrOrdem(1).dsStatus(StatusPlanoItem.PENDENTE).build();
        PlanoItem item2 = PlanoItem.builder().idItem(2L).plano(plano).nrOrdem(2).dsStatus(StatusPlanoItem.AGENDADO).evento(evento).build();

        when(eventoSaudeRepository.findById(10L)).thenReturn(Optional.of(evento));
        when(eventoSaudeRepository.save(any(EventoSaude.class))).thenAnswer(inv -> inv.getArgument(0));
        when(planoItemRepository.findByEvento_IdEvento(10L)).thenReturn(Optional.of(item2));
        when(planoItemRepository.findByPlano_IdPlanoOrderByNrOrdemAsc(100L)).thenReturn(List.of(item1, item2));

        eventoService.concluir(10L, "Obs", BigDecimal.ZERO);

        assertThat(plano.getDsStatus()).isEqualTo(StatusPlanoTratamento.QUEBRADO);
        verify(planoTratamentoRepository).save(plano);
        verify(pontosService, never()).lancarBonusPendente(any());
    }

    @Test
    @DisplayName("Deve cancelar evento com reagendamento opcional")
    void cancelar_ComReagendamento() {
        Pet pet = Pet.builder().idPet(1L).build();
        TipoEvento tipo = TipoEvento.builder().idTipoEvento(2L).build();
        Veterinario vet = Veterinario.builder().idVeterinario(3L).build();

        EventoSaude evento = EventoSaude.builder()
                .idEvento(10L)
                .pet(pet)
                .tipoEvento(tipo)
                .veterinario(vet)
                .dsStatus(StatusEvento.AGENDADO)
                .build();

        when(eventoSaudeRepository.findById(10L)).thenReturn(Optional.of(evento));
        when(eventoSaudeRepository.save(any(EventoSaude.class))).thenAnswer(inv -> inv.getArgument(0));
        when(planoItemRepository.findByEvento_IdEvento(10L)).thenReturn(Optional.empty());

        LocalDate novaData = LocalDate.now().plusWeeks(1);
        EventoService.ResultadoCancelamento resultado = eventoService.cancelar(10L, "Imprevisto pessoal", novaData);

        assertThat(resultado.eventoCancelado().getDsStatus()).isEqualTo(StatusEvento.CANCELADO);
        assertThat(resultado.eventoCancelado().getDsMotivoCancelamento()).isEqualTo("Imprevisto pessoal");
        assertThat(resultado.novoEvento()).isNotNull();
        assertThat(resultado.novoEvento().getDsStatus()).isEqualTo(StatusEvento.AGENDADO);
        assertThat(resultado.novoEvento().getDtEvento()).isEqualTo(novaData);
    }

    @Test
    @DisplayName("Deve calcular gasto total somando somente eventos CONCLUIDOS")
    void calcularGastoTotal() {
        EventoSaude e1 = EventoSaude.builder().dsStatus(StatusEvento.CONCLUIDO).vlCusto(new BigDecimal("100.00")).build();
        EventoSaude e2 = EventoSaude.builder().dsStatus(StatusEvento.CONCLUIDO).vlCusto(new BigDecimal("50.50")).build();
        EventoSaude e3 = EventoSaude.builder().dsStatus(StatusEvento.CANCELADO).vlCusto(new BigDecimal("200.00")).build();

        when(eventoSaudeRepository.findByPet_IdPet(1L)).thenReturn(List.of(e1, e2, e3));

        BigDecimal total = eventoService.calcularGastoTotal(1L);
        assertThat(total).isEqualByComparingTo("150.50");
    }

    @Test
    @DisplayName("Deve gerar alertas identificando evento atrasado se >= 12 meses")
    void gerarAlertas() {
        TipoEvento vacina = TipoEvento.builder().nmTipoEvento("Vacina").build();
        EventoSaude evAntigo = EventoSaude.builder()
                .tipoEvento(vacina)
                .dsStatus(StatusEvento.CONCLUIDO)
                .dtEvento(LocalDate.now().minusMonths(14))
                .build();

        when(eventoSaudeRepository.findByPet_IdPet(1L)).thenReturn(List.of(evAntigo));

        List<EventoService.AlertaEvento> alertas = eventoService.gerarAlertas(1L);
        assertThat(alertas).hasSize(1);
        assertThat(alertas.get(0).atrasado()).isTrue();
        assertThat(alertas.get(0).mensagem()).contains("pode estar atrasado");
    }
}
