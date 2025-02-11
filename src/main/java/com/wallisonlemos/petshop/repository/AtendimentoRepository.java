package com.wallisonlemos.petshop.repository;

import com.wallisonlemos.petshop.model.domain.atendimento.Atendimento;
import com.wallisonlemos.petshop.model.domain.pet.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AtendimentoRepository extends JpaRepository<Atendimento, Integer> {
    Optional<Atendimento> findByPet(Pet pet);
}
