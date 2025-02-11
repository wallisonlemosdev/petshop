package com.wallisonlemos.petshop.model.domain.usuario.enums;

import com.wallisonlemos.petshop.config.exception.PetshopException;

public enum Perfil {

    ADMIN,
    CLIENTE;

    public static Perfil fromString(String value) {
        try {
            return Perfil.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new PetshopException("Erro", "Perfil inválido: " + value);
        }
    }
}
