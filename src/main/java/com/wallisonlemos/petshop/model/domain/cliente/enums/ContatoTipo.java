package com.wallisonlemos.petshop.model.domain.cliente.enums;

import com.wallisonlemos.petshop.config.exception.PetshopException;

public enum ContatoTipo {
    EMAIL,
    TELEFONE;

    public static ContatoTipo fromString(String value) {
        try {
            return ContatoTipo.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new PetshopException("Erro", "Tipo de contato inválido: " + value);
        }
    }
}
