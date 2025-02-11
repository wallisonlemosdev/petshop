package com.wallisonlemos.petshop.controller;

import com.wallisonlemos.petshop.config.exception.PetshopException;
import com.wallisonlemos.petshop.config.exception.PetshopExceptionDto;
import com.wallisonlemos.petshop.model.dto.atendimento.AtendimentoCreateDto;
import com.wallisonlemos.petshop.model.dto.atendimento.AtendimentoResponseDto;
import com.wallisonlemos.petshop.model.dto.atendimento.AtendimentoUpdateDto;
import com.wallisonlemos.petshop.service.AtendimentoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/atendimentos")
public class AtendimentoController {

    private final AtendimentoService service;

    @Autowired
    public AtendimentoController(AtendimentoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Object> getAll(@PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        try {
            Page<AtendimentoResponseDto> atendimentos = service.findAll(pageable);
            return ResponseEntity.ok(atendimentos);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new PetshopExceptionDto("Erro ao listar atendimentos", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable Integer id) {
        try {
            AtendimentoResponseDto atendimento = service.findById(id);
            return ResponseEntity.ok(atendimento);
        } catch (PetshopException e) {
            return ResponseEntity.badRequest()
                    .body(new PetshopExceptionDto(e.getErro(), e.getMensagem()));
        }
    }

    @PostMapping()
    public ResponseEntity<Object> create(@RequestBody @Valid AtendimentoCreateDto createDto) {
        try {
            AtendimentoResponseDto created = service.create(createDto);
            return ResponseEntity.status(201).body(created);
        } catch (PetshopException e) {
            return ResponseEntity.badRequest()
                    .body(new PetshopExceptionDto(e.getErro(), e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Integer id, @RequestBody @Valid AtendimentoUpdateDto updateDto) {
        try {
            AtendimentoResponseDto updatedPet = service.update(id, updateDto);
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