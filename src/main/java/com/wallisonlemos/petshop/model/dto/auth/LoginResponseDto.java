package com.wallisonlemos.petshop.model.dto.auth;

public record LoginResponseDto(
        Integer id,
        String cpf,
        String nome,
        String token) {}
