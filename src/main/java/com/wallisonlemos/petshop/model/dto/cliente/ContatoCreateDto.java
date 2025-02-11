package com.wallisonlemos.petshop.model.dto.cliente;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContatoCreateDto {

    private String tag;

    @NotBlank(message = "É obrigatório informar o tipo de contato")
    private String tipo;

    @NotBlank(message = "É obrigatório informar o contato")
    private String valor;
}
