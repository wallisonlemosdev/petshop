package com.wallisonlemos.petshop.model.dto.auth;

import com.wallisonlemos.petshop.model.domain.usuario.enums.Perfil;

public record UsuarioResponseDto(String nome, String cpf, Perfil perfil) {}
