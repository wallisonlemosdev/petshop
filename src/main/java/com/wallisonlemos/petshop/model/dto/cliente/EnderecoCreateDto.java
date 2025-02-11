package com.wallisonlemos.petshop.model.dto.cliente;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnderecoCreateDto {

    @NotBlank(message = "É obrigatório informar o logradouro")
    private String logradouro;

    @NotBlank(message = "É obrigatório informar a cidade")
    private String cidade;

    @NotBlank(message = "É obrigatório informar o bairro")
    private String bairro;

    private String complemento;

    private String tag;
}