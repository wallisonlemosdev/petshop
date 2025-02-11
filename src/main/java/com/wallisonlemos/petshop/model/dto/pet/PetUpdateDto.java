package com.wallisonlemos.petshop.model.dto.pet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class PetUpdateDto {

    private String nome;
    private LocalDate dataNascimento;
    private String racaDescricao;
}
