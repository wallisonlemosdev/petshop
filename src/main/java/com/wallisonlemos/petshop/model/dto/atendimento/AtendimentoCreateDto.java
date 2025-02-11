package com.wallisonlemos.petshop.model.dto.atendimento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AtendimentoCreateDto {

    @NotNull(message = "É obrigatório informar um Pet")
    private Integer petId;

    @NotBlank(message = "É obrigatório informar a descrição do atendimento")
    private String descricao;

    @NotNull(message = "É obrigatório informar um valor para o atendimento")
    private BigDecimal valor;
}