package com.wallisonlemos.petshop.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BemVindoController {

    @GetMapping
    public String bemVindo() {
        return "Bem vindo à minha aplicação Petshop";
    }

    @GetMapping("/clientes")
    public String usuarios() {
        return "Cliente autorizado!";
    }

    @GetMapping("/administradores")
    public String administradores() {
        return "Admin autorizado!";
    }
}
