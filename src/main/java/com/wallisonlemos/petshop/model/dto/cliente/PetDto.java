package com.wallisonlemos.petshop.model.dto.cliente;

import java.time.LocalDate;

public record PetDto(
        Integer id,
        String nome,
        String raca,
        LocalDate dataNascimento
) {
}
