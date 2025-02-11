package com.wallisonlemos.petshop.controller;

import com.wallisonlemos.petshop.config.exception.PetshopException;
import com.wallisonlemos.petshop.config.exception.PetshopExceptionDto;
import com.wallisonlemos.petshop.model.dto.auth.LoginDto;
import com.wallisonlemos.petshop.model.dto.auth.LoginResponseDto;
import com.wallisonlemos.petshop.model.dto.auth.UsuarioResponseDto;
import com.wallisonlemos.petshop.model.dto.cliente.*;
import com.wallisonlemos.petshop.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("auth")
public class UsuarioController {

    final
    UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody @Valid LoginDto loginDto){
        try {
            LoginResponseDto token = service.login(loginDto);
            return ResponseEntity.ok(token);
        } catch (AuthenticationException exception) {
            return ResponseEntity
                    .badRequest()
                    .body(new PetshopExceptionDto("Não autorizado!",
                            "Verifique o email ou senha e tente novamente!"));
        }
    }

    @GetMapping("/clientes")
    public ResponseEntity<Object> getAll(@PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        try {
            Page<ClienteResponseDto> clientes = service.findAll(pageable);
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new PetshopExceptionDto("Erro ao listar clientes", e.getMessage()));
        }
    }

    @GetMapping("/clientes/com-pets")
    public ResponseEntity<Object> getWithPets() {
        try {
            List<ClienteWithPetResponseDto> clientes = service.findAllWithPets();
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new PetshopExceptionDto("Erro ao listar clientes com pets", e.getMessage()));
        }
    }

    @PostMapping()
    public ResponseEntity<Object> create(@Valid @RequestBody ClienteCreateDto cadastroDto){
        try {
            UsuarioResponseDto usuarioResponseDto = service.create(cadastroDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioResponseDto);
        } catch (PetshopException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new PetshopExceptionDto(e.getErro(), e.getMensagem()));
        }
    }

    @GetMapping("/clientes/{id}")
    public ResponseEntity<Object> getById(@PathVariable Integer id) {
        try {
            ClienteDto pet = service.findById(id);
            return ResponseEntity.ok(pet);
        } catch (PetshopException e) {
            return ResponseEntity.badRequest()
                    .body(new PetshopExceptionDto(e.getErro(), e.getMensagem()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Integer id, @Valid @RequestBody ClienteUpdateDto edicaoDto) {
        try {
            UsuarioResponseDto usuarioResponseDto = service.update(id, edicaoDto);
            return ResponseEntity.ok(usuarioResponseDto);
        } catch (PetshopException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new PetshopExceptionDto(e.getErro(), e.getMensagem()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (PetshopException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new PetshopExceptionDto(e.getErro(), e.getMensagem()));
        }
    }
}
