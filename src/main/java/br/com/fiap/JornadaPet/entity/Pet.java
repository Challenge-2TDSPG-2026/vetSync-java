package br.com.fiap.JornadaPet.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
@Table(name = "pets")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "Espécie é obrigatória")
    private String especie;

    private String raca;

    @Positive(message = "Peso deve ser positivo")
    private Double peso;

    @NotNull(message = "Data de nascimento é obrigatória")
    private LocalDate dataNascimento;

    private String sexo;

    private boolean castrado;

    private String observacoes;

    @ManyToOne
    @JoinColumn(name = "tutor_id")
    private Tutor tutor;

}