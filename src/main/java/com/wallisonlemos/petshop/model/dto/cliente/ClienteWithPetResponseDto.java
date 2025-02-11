package com.wallisonlemos.petshop.model.dto.cliente;

import java.util.List;

public record ClienteWithPetResponseDto(Integer id, String nome, String cpf, List<PetDto> pets) {}
