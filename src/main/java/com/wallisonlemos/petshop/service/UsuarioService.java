package com.wallisonlemos.petshop.service;

import com.wallisonlemos.petshop.config.exception.PetshopException;
import com.wallisonlemos.petshop.config.security.TokenService;
import com.wallisonlemos.petshop.model.domain.cliente.Cliente;
import com.wallisonlemos.petshop.model.domain.cliente.Contato;
import com.wallisonlemos.petshop.model.domain.cliente.Endereco;
import com.wallisonlemos.petshop.model.domain.cliente.enums.ContatoTipo;
import com.wallisonlemos.petshop.model.domain.usuario.Usuario;
import com.wallisonlemos.petshop.model.domain.usuario.enums.Perfil;
import com.wallisonlemos.petshop.model.dto.auth.LoginDto;
import com.wallisonlemos.petshop.model.dto.auth.LoginResponseDto;
import com.wallisonlemos.petshop.model.dto.auth.UsuarioResponseDto;
import com.wallisonlemos.petshop.model.dto.cliente.*;
import com.wallisonlemos.petshop.repository.ClienteRepository;
import com.wallisonlemos.petshop.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService implements UserDetailsService {

    private final ApplicationContext context;
    private final UsuarioRepository repository;
    private final ClienteRepository clienteRepository;
    private final TokenService tokenService;

    public UsuarioService(ApplicationContext context, UsuarioRepository repository, ClienteRepository clienteRepository, TokenService tokenService) {
        this.context = context;
        this.repository = repository;
        this.clienteRepository = clienteRepository;
        this.tokenService = tokenService;
    }

    @Override
    public UserDetails loadUserByUsername(String cpf) throws UsernameNotFoundException {
        return repository.findByCpf(cpf);
    }

    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new PetshopException("Erro", "Usuário não autenticado");
        }

        String cpf = authentication.getName();

        return repository.findUserByCpf(cpf)
                .orElseThrow(() -> new PetshopException("Erro", "Usuário autenticado não encontrado"));
    }

    public LoginResponseDto login(@RequestBody @Valid LoginDto data){
        AuthenticationManager authenticationManager = context.getBean(AuthenticationManager.class);
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.cpf(), data.password());
        var auth = authenticationManager.authenticate(usernamePassword);
        var usuario = (Usuario) auth.getPrincipal();
        var token = tokenService.generateToken(usuario);

        String nome = usuario.getNome();
        return new LoginResponseDto(usuario.getId(), usuario.getUsername(), nome, token);
    }

    @Transactional
    public UsuarioResponseDto create(ClienteCreateDto cadastroDto) {

        if (this.repository.findByCpf(cadastroDto.getCpf()) != null) {
            throw new PetshopException("Erro", String.format("O CPF %s já está em uso.", cadastroDto.getCpf()));
        }

        if (!cadastroDto.getNome().contains(" ")) {
            throw new PetshopException("Erro", "O nome deve conter pelo menos nome e sobrenome.");
        }

        if (!cadastroDto.getPassword().matches("^(?=.*[a-zA-Z])(?=.*\\d).{8,}$")) {
            throw new PetshopException("Erro", "A senha deve conter pelo menos uma letra e um número, com no mínimo 8 caracteres.");
        }

        Cliente cliente = new Cliente();
        cliente.setCpf(cadastroDto.getCpf());
        cliente.setNome(cadastroDto.getNome());

        Endereco endereco = new Endereco(
                cadastroDto.getEndereco().getLogradouro(),
                cadastroDto.getEndereco().getCidade(),
                cadastroDto.getEndereco().getBairro(),
                cadastroDto.getEndereco().getComplemento(),
                cadastroDto.getEndereco().getTag(),
                cliente);

        Contato contato = new Contato(
                cadastroDto.getContato().getTag(),
                ContatoTipo.fromString(cadastroDto.getContato().getTipo()),
                cadastroDto.getContato().getValor(),
                cliente);

        if ("EMAIL".equals(cadastroDto.getContato().getTipo()) && !cadastroDto.getContato().getValor().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new PetshopException("Erro", "Formato de email inválido");
        }

        cliente.setEndereco(endereco);
        cliente.setContato(contato);

        String senhaEncriptada = new BCryptPasswordEncoder().encode(cadastroDto.getPassword());
        Usuario usuario = new Usuario(
                cadastroDto.getCpf(),
                cadastroDto.getNome(),
                senhaEncriptada,
                Perfil.fromString(cadastroDto.getPerfil()));

        usuario.setCliente(cliente);

        this.repository.save(usuario);
        return new UsuarioResponseDto(usuario.getNome(), usuario.getCpf(), usuario.getPerfil());
    }

    @Transactional
    public UsuarioResponseDto update(Integer id, ClienteUpdateDto edicaoDto) {
        Usuario usuarioAutenticado = getUsuarioAutenticado();

        if (!(usuarioAutenticado.getPerfil().equals(Perfil.ADMIN) || usuarioAutenticado.getCliente().getId().equals(id))) {
            throw new PetshopException("Permissão negada", "Você só pode editar seus próprios dados.");
        }

        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new PetshopException("Erro", "Usuário não encontrado"));

        Cliente cliente = usuario.getCliente();

        if (edicaoDto.getNome() != null && !edicaoDto.getNome().isBlank()) {
            if (!edicaoDto.getNome().contains(" ")) {
                throw new PetshopException("Erro", "O nome deve conter pelo menos nome e sobrenome.");
            }
            usuario.setNome(edicaoDto.getNome());
            cliente.setNome(edicaoDto.getNome());
        }

        if (edicaoDto.getPerfil() != null) {
            Perfil novoPerfil = Perfil.fromString(edicaoDto.getPerfil());

            if (Perfil.ADMIN == novoPerfil && Perfil.ADMIN != usuarioAutenticado.getPerfil()) {
                throw new PetshopException("Erro", "Apenas administradores podem alterar perfis de usuário!");
            }

            usuario.setPerfil(novoPerfil);
        }

        if (edicaoDto.getEndereco() != null) {
            cliente.getEndereco().setLogradouro(edicaoDto.getEndereco().getLogradouro());
            cliente.getEndereco().setCidade(edicaoDto.getEndereco().getCidade());
            cliente.getEndereco().setBairro(edicaoDto.getEndereco().getBairro());
            cliente.getEndereco().setComplemento(edicaoDto.getEndereco().getComplemento());
            cliente.getEndereco().setTag(edicaoDto.getEndereco().getTag());
        }

        if (edicaoDto.getContato() != null) {
            ContatoTipo tipo = ContatoTipo.fromString(edicaoDto.getContato().getTipo());

            if (tipo == ContatoTipo.EMAIL && !edicaoDto.getContato().getValor().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                throw new PetshopException("Erro", "Formato de email inválido");
            }

            cliente.getContato().setTag(edicaoDto.getContato().getTag());
            cliente.getContato().setTipo(ContatoTipo.fromString(edicaoDto.getContato().getTipo()));
            cliente.getContato().setValor(edicaoDto.getContato().getValor());
        }

        if (cliente.getEndereco() == null) {
            throw new PetshopException("Erro", "Endereço não pode ser nulo");
        }

        if (cliente.getContato() == null) {
            throw new PetshopException("Erro", "Contato não pode ser nulo");
        }

        repository.save(usuario);
        return new UsuarioResponseDto(usuario.getNome(), usuario.getCpf(), usuario.getPerfil());
    }

    @Transactional
    public void delete(Integer id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new PetshopException("Erro", "Usuário não encontrado"));

        repository.delete(usuario);
    }

    public Page<ClienteResponseDto> findAll(Pageable pageable) {
            Usuario usuario = getUsuarioAutenticado();
            List<Cliente> clientes;

            if (usuario.getPerfil().equals(Perfil.ADMIN)) {
                clientes = clienteRepository.findAll(pageable).getContent();
            } else {
                Cliente cliente = usuario.getCliente();
                if (cliente == null) {
                    throw new PetshopException("Acesso negado", "Cliente não vinculado ao usuário.");
                }
                clientes = List.of(usuario.getCliente());
            }

            List<ClienteResponseDto> clienteResponseDtos = clientes.stream()
                    .map(clie -> new ClienteResponseDto(
                            clie.getId(),
                            clie.getNome(),
                            clie.getCpf()))
                    .collect(Collectors.toList());

            return new PageImpl<>(clienteResponseDtos, pageable, clientes.size());
    }

    public List<ClienteWithPetResponseDto> findAllWithPets() {
        Usuario usuario = getUsuarioAutenticado();
        List<Cliente> clientes;

        if (!usuario.getPerfil().equals(Perfil.ADMIN)) {
            throw new PetshopException("Acesso negado", "Apenas administradores podem listar clientes.");

        }

        clientes = clienteRepository.findAll();
        return clientes.stream()
                .filter(cli -> cli.getPets() != null && !cli.getPets().isEmpty())
                .map(cli -> new ClienteWithPetResponseDto(
                        cli.getId(),
                        cli.getNome(),
                        cli.getCpf(),
                        cli.getPets().stream()
                                .map(pet -> new PetDto(
                                        pet.getId(),
                                        pet.getNome(),
                                        pet.getRaca().getDescricao(),
                                        pet.getDataNascimento()
                                ))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    public ClienteDto findById(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new PetshopException("Cliente não encontrado com o ID: " + id));

        List<PetDto> pets = cliente.getPets()
                .stream().map(pet -> new PetDto(
                        pet.getId(),
                        pet.getNome(),
                        pet.getRaca().getDescricao(),
                        pet.getDataNascimento()))
                .collect(Collectors.toList());

        return new ClienteDto(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getUsuario().getPerfil().toString(),
                cliente.getEndereco().getLogradouro(),
                cliente.getEndereco().getBairro(),
                cliente.getEndereco().getCidade(),
                cliente.getEndereco().getComplemento(),
                cliente.getEndereco().getTag(),
                cliente.getContato().getTipo().toString(),
                cliente.getContato().getValor(),
                cliente.getContato().getTag(),
                pets);
    }
}
