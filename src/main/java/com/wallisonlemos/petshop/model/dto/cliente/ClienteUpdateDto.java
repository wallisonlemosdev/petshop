package com.wallisonlemos.petshop.model.dto.cliente;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ClienteUpdateDto {

    private String nome;

    private String perfil;

    private EnderecoCreateDto endereco;

    private ContatoCreateDto contato;
}