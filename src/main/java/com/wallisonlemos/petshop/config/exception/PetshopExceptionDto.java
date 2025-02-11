package com.wallisonlemos.petshop.config.exception;

import lombok.Getter;

@Getter
public class PetshopExceptionDto {

    private String erro;
    private String mensagem;

    @Deprecated
    public PetshopExceptionDto() {
    }

    public PetshopExceptionDto(String mensagem) {
        this.erro = "Erro";
        this.mensagem = mensagem;
    }

    public PetshopExceptionDto(String erro, String mensagem) {
        this.erro = erro;
        this.mensagem = mensagem;
    }
}