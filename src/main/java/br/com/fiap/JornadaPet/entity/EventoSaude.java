package br.com.fiap.JornadaPet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "eventos_saude")
public class EventoSaude {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotNull(message = "Tipo do evento é obrigatório")
    @Enumerated(EnumType.STRING)
    private TipoEvento tipo;


    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusEvento status = StatusEvento.PENDENTE;

    private String descricao;

    private LocalDate dataRealizacao;

    private LocalDate dataProxima; // próxima dose / próximo banho / etc.

    @ManyToOne
    @JoinColumn(name = "pet_id")
    private Pet pet;

    public enum TipoEvento {
        VACINA, VERMIFUGO, BANHO, TOSA, CHECKUP, CONSULTA, CIRURGIA, MEDICAMENTO
    }

    public enum StatusEvento {
        PENDENTE, REALIZADO, ATRASADO
    }

}