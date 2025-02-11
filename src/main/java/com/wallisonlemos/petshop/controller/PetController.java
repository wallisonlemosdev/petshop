package com.wallisonlemos.petshop.controller;

import com.wallisonlemos.petshop.config.exception.PetshopException;
import com.wallisonlemos.petshop.config.exception.PetshopExceptionDto;
import com.wallisonlemos.petshop.model.dto.pet.PetCreateDto;
import com.wallisonlemos.petshop.model.dto.pet.PetResponseDto;
import com.wallisonlemos.petshop.model.dto.pet.PetUpdateDto;
import com.wallisonlemos.petshop.service.PetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pets")
public class PetController {

    private final PetService service;

    @Autowired
    public PetController(PetService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Object> getAll(@PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        try {
            Page<PetResponseDto> pets = service.findAll(pageable);
            return ResponseEntity.ok(pets);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new PetshopExceptionDto("Erro ao listar pets", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable Integer id) {
        try {
            PetResponseDto pet = service.findById(id);
            return ResponseEntity.ok(pet);
        } catch (PetshopException e) {
            return ResponseEntity.badRequest()
                    .body(new PetshopExceptionDto(e.getErro(), e.getMensagem()));
        }
    }

    @PostMapping()
    public ResponseEntity<Object> create(@RequestBody @Valid PetCreateDto pet) {
        try {
            PetResponseDto createdPet = service.create(pet);
            return ResponseEntity.status(201).body(createdPet);
        } catch (PetshopException e) {
            return ResponseEntity.badRequest()
                    .body(new PetshopExceptionDto(e.getErro(), e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody @Valid PetUpdateDto pet) {
        try {
            PetResponseDto updatedPet = service.update(id, pet);
            return ResponseEntity.ok(updatedPet);
        } catch (PetshopException e) {
            return ResponseEntity.badRequest()
                    .body(new PetshopExceptionDto(e.getErro(), e.getMensagem()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (PetshopException e) {
            return ResponseEntity.badRequest()
                    .body(new PetshopExceptionDto(e.getErro(), e.getMensagem()));
        }
    }
}