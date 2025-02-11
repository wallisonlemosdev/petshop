package com.wallisonlemos.petshop.model.dto.pet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PetCreateDto {

    @NotBlank(message = "É obrigatório informar o nome do pet")
    private String nome;

    private LocalDate dataNascimento;

    @NotBlank(message = "É obrigatório descrever a raça")
    private String racaDescricao;

    @NotNull(message = "Obrigatório informar o cliente")
    private Integer clienteId;
}
