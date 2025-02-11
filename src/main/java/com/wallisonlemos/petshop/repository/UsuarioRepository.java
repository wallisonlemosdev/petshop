package com.wallisonlemos.petshop.repository;

import com.wallisonlemos.petshop.model.domain.cliente.Cliente;
import com.wallisonlemos.petshop.model.domain.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    UserDetails findByCpf(String cpf);

    Optional<Usuario> findUserByCpf(String cpf);

    List<Cliente> findByCliente(Cliente cliente);
}
