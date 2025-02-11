package com.wallisonlemos.petshop.model.dto.cliente;

import java.util.List;

public record ClienteDto(
        Integer id,
        String nome,
        String cpf,
        String perfil,
        String rua,
        String bairro,
        String cidade,
        String complemento,
        String tagEndereco,
        String tipoContato,
        String valorContato,
        String tagContato,
        List<PetDto> pets) {
}
