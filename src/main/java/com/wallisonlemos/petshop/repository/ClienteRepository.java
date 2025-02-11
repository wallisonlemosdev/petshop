package com.wallisonlemos.petshop.repository;

import com.wallisonlemos.petshop.model.domain.cliente.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
}
