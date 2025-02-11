package com.wallisonlemos.petshop.repository;

import com.wallisonlemos.petshop.model.domain.cliente.Contato;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContatoRepository extends JpaRepository<Contato, Integer> {}
