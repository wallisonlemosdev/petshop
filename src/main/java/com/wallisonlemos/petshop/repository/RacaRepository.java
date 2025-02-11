package com.wallisonlemos.petshop.repository;

import com.wallisonlemos.petshop.model.domain.pet.Raca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RacaRepository extends JpaRepository<Raca, Integer> {
    Optional<Raca> findByDescricao(String racaDescricao);
}
