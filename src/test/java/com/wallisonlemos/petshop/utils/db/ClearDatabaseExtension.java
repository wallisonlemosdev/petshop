package com.wallisonlemos.petshop.utils.db;

import com.wallisonlemos.petshop.model.domain.cliente.Cliente;
import com.wallisonlemos.petshop.model.domain.cliente.Contato;
import com.wallisonlemos.petshop.model.domain.cliente.Endereco;
import com.wallisonlemos.petshop.model.domain.cliente.enums.ContatoTipo;
import com.wallisonlemos.petshop.model.domain.usuario.Usuario;
import com.wallisonlemos.petshop.model.domain.usuario.enums.Perfil;
import com.wallisonlemos.petshop.repository.UsuarioRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class ClearDatabaseExtension implements BeforeAllCallback {

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        Flyway flyway = SpringExtension.getApplicationContext(extensionContext)
                .getBean(Flyway.class);
        UsuarioRepository usuariosRepository = SpringExtension.getApplicationContext(extensionContext)
                .getBean(UsuarioRepository.class);

        flyway.clean();
        flyway.migrate();
        saveUsuario(usuariosRepository);
    }

    public void saveUsuario(UsuarioRepository usuarioRepository) {
        String senhaEncriptada = new BCryptPasswordEncoder().encode("Pet0123456");

        Usuario usuario = new Usuario("06777953000", "João Silva", senhaEncriptada, Perfil.ADMIN);
        Cliente cliente = new Cliente(usuario);
        cliente.setEndereco(new Endereco("Rua central", "Serra Talhada", "Centro", "Lado A", "Residencia", cliente));
        cliente.setContato(new Contato("Pessoal", ContatoTipo.TELEFONE, "(87)98111-1111", cliente));
        usuario.setCliente(cliente);

        usuarioRepository.save(usuario);
    }
}
