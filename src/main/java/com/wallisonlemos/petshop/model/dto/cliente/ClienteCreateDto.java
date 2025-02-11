package com.wallisonlemos.petshop.model.dto.cliente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ClienteCreateDto {

    @NotBlank(message = "É obrigatório informar um CPF válido")
    @CPF(message = "O CPF informado não é válido")
    private String cpf;

    @NotBlank(message = "É obrigatório informar o nome completo")
    private String nome;

    @NotBlank(message = "É obrigatório informar uma senha")
    @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    private String password;

    @NotBlank(message = "É obrigatório informar um perfil de usuário")
    private String perfil;

    @NotNull(message = "É necessário informar um endereço")
    private EnderecoCreateDto endereco;

    @NotNull(message = "É necessário informar um contato")
    private ContatoCreateDto contato;
}