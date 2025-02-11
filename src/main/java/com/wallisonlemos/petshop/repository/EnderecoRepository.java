package com.wallisonlemos.petshop.repository;

import com.wallisonlemos.petshop.model.domain.cliente.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnderecoRepository extends JpaRepository<Endereco, Integer> {}
