package com.wallisonlemos.petshop.config.exception;

import lombok.Getter;

@Getter
public class PetshopException extends RuntimeException {

    private final String erro;
    private final String mensagem;

    public PetshopException(String erro, String mensagem) {
        super(mensagem);
        this.erro = erro;
        this.mensagem = mensagem;
    }

    public PetshopException(String mensagem) {
        this("Erro", mensagem);
    }
}
