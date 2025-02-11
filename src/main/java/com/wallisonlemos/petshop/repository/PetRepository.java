package com.wallisonlemos.petshop.repository;

import com.wallisonlemos.petshop.model.domain.cliente.Cliente;
import com.wallisonlemos.petshop.model.domain.pet.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Integer> {
    List<Pet> findByCliente(Cliente cliente);
}
