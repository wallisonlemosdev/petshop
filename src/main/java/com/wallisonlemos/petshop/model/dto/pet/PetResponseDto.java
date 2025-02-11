package com.wallisonlemos.petshop.model.dto.pet;

import java.time.LocalDate;

public record PetResponseDto(Integer id, String nome, String raca, LocalDate dataNascimento, String cliente) {}
