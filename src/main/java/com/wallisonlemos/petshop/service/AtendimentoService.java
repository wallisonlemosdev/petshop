package com.wallisonlemos.petshop.service;

import com.wallisonlemos.petshop.config.exception.PetshopException;
import com.wallisonlemos.petshop.model.domain.atendimento.Atendimento;
import com.wallisonlemos.petshop.model.domain.cliente.Cliente;
import com.wallisonlemos.petshop.model.domain.pet.Pet;
import com.wallisonlemos.petshop.model.domain.usuario.Usuario;
import com.wallisonlemos.petshop.model.domain.usuario.enums.Perfil;
import com.wallisonlemos.petshop.model.dto.atendimento.AtendimentoCreateDto;
import com.wallisonlemos.petshop.model.dto.atendimento.AtendimentoResponseDto;
import com.wallisonlemos.petshop.model.dto.atendimento.AtendimentoUpdateDto;
import com.wallisonlemos.petshop.repository.AtendimentoRepository;
import com.wallisonlemos.petshop.repository.PetRepository;
import com.wallisonlemos.petshop.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AtendimentoService {

    private final AtendimentoRepository repository;
    private final PetRepository petRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public AtendimentoService(AtendimentoRepository repository, PetRepository petRepository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.petRepository = petRepository;
        this.usuarioRepository = usuarioRepository;
    }

    private Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new PetshopException("Erro", "Usuário não autenticado");
        }

        String cpf = authentication.getName();

        return usuarioRepository.findUserByCpf(cpf)
                .orElseThrow(() -> new PetshopException("Erro", "Usuário autenticado não encontrado"));
    }

    public Page<AtendimentoResponseDto> findAll(Pageable pageable) {
        Usuario usuario = getUsuarioAutenticado();
        List<Atendimento> atendimentos;

        if (usuario.getPerfil().equals(Perfil.ADMIN)) {
            atendimentos = repository.findAll(pageable).getContent();
        } else {
            Cliente cliente = usuario.getCliente();
            if (cliente == null) {
                throw new PetshopException("Acesso negado", "Cliente não vinculado ao usuário.");
            }

            atendimentos = cliente.getPets().stream()
                    .flatMap(pet -> repository.findByPet(pet).stream())
                    .toList();
        }

        return atendimentos.stream()
                .map(at -> new AtendimentoResponseDto(
                        at.getId(),
                        at.getPet().getCliente().getNome(),
                        at.getPet().getId(),
                        at.getPet().getNome(),
                        at.getDescricao(),
                        at.getValor(),
                        at.getCreatedAt().toLocalDate()))
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        list -> new PageImpl<>(list, pageable, list.size())));
    }

    public AtendimentoResponseDto findById(Integer id) {
        Usuario usuario = getUsuarioAutenticado();
        Atendimento atendimento = repository.findById(id)
                .orElseThrow(() -> new PetshopException("Atendimento não encontrado com o ID: " + id));

        if (!usuario.getPerfil().equals(Perfil.ADMIN)) {
            Cliente clienteDoPet = atendimento.getPet().getCliente();
            if (clienteDoPet == null || !clienteDoPet.getUsuario().equals(usuario)) {
                throw new PetshopException(
                        "Permissão negada",
                        "Você só pode visualizar seus próprios atendimentos."
                );
            }
        }

        return new AtendimentoResponseDto(
                atendimento.getId(),
                atendimento.getPet().getCliente().getNome(),
                atendimento.getPet().getId(),
                atendimento.getPet().getNome(),
                atendimento.getDescricao(),
                atendimento.getValor(),
                atendimento.getCreatedAt().toLocalDate());
    }

    @Transactional
    public AtendimentoResponseDto create(AtendimentoCreateDto createDto) {
        Pet pet = petRepository.findById(createDto.getPetId())
                .orElseThrow(() -> new PetshopException("Erro", "Pet não encontrado"));

        BigDecimal valor = createDto.getValor().setScale(2, RoundingMode.HALF_UP);

        Atendimento atendimento = new Atendimento(pet, createDto.getDescricao(), valor);

        atendimento = repository.save(atendimento);
        return new AtendimentoResponseDto(
                atendimento.getId(),
                atendimento.getPet().getCliente().getNome(),
                atendimento.getPet().getId(),
                atendimento.getPet().getNome(),
                atendimento.getDescricao(),
                atendimento.getValor(),
                atendimento.getCreatedAt().toLocalDate());
    }

    @Transactional
    public AtendimentoResponseDto update(Integer id, AtendimentoUpdateDto updatedPet) {
        Atendimento atendimento = repository.findById(id)
                .orElseThrow(() -> new PetshopException("Erro", "Atendimento não encontrado"));

        if(updatedPet.getValor() != null) {
            atendimento.setValor(updatedPet.getValor().setScale(2, RoundingMode.HALF_UP));
        }

        if(updatedPet.getDescricao() != null && !updatedPet.getDescricao().isBlank()) {
            atendimento.setDescricao(updatedPet.getDescricao());
        }

        atendimento = repository.save(atendimento);
        return new AtendimentoResponseDto(
                atendimento.getId(),
                atendimento.getPet().getCliente().getNome(),
                atendimento.getPet().getId(),
                atendimento.getPet().getNome(),
                atendimento.getDescricao(),
                atendimento.getValor(),
                atendimento.getCreatedAt().toLocalDate());
    }

    @Transactional
    public void delete(Integer id) {
        Atendimento atendimento = repository.findById(id)
                .orElseThrow(() -> new PetshopException("Erro", "Atendimento não encontrado"));

        repository.delete(atendimento);
    }
}
