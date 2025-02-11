package com.wallisonlemos.petshop.model.dto.atendimento;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class AtendimentoUpdateDto {

    private String descricao;
    private BigDecimal valor;
}