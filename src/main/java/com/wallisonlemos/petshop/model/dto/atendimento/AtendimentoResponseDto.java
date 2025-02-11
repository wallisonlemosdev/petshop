package com.wallisonlemos.petshop.model.dto.atendimento;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AtendimentoResponseDto(
        Integer id,
        String clienteNome,
        Integer petId,
        String petNome,
        String descricao,
        BigDecimal valor,
        LocalDate data
) {
}
