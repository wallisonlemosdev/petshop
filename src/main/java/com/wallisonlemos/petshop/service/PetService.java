package com.wallisonlemos.petshop.service;

import com.wallisonlemos.petshop.config.exception.PetshopException;
import com.wallisonlemos.petshop.model.dto.pet.PetResponseDto;
import com.wallisonlemos.petshop.model.domain.cliente.Cliente;
import com.wallisonlemos.petshop.model.domain.pet.Pet;
import com.wallisonlemos.petshop.model.domain.pet.Raca;
import com.wallisonlemos.petshop.model.domain.usuario.Usuario;
import com.wallisonlemos.petshop.model.domain.usuario.enums.Perfil;
import com.wallisonlemos.petshop.model.dto.pet.PetCreateDto;
import com.wallisonlemos.petshop.model.dto.pet.PetUpdateDto;
import com.wallisonlemos.petshop.repository.ClienteRepository;
import com.wallisonlemos.petshop.repository.PetRepository;
import com.wallisonlemos.petshop.repository.RacaRepository;
import com.wallisonlemos.petshop.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PetService {

    private final PetRepository repository;
    private final RacaRepository racaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public PetService(PetRepository repository, RacaRepository racaRepository, ClienteRepository clienteRepository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.racaRepository = racaRepository;
        this.clienteRepository = clienteRepository;
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

    public Page<PetResponseDto> findAll(Pageable pageable) {
        Usuario usuario = getUsuarioAutenticado();
        List<Pet> pets;

        if (usuario.getPerfil().equals(Perfil.ADMIN)) {
            pets = repository.findAll(pageable).getContent();
        } else {
            Cliente cliente = usuario.getCliente();
            if (cliente == null) {
                throw new PetshopException("Acesso negado", "Cliente não vinculado ao usuário.");
            }
            pets = repository.findByCliente(cliente);
        }

        List<PetResponseDto> petResponseDtos = pets.stream()
                .map(pet -> new PetResponseDto(
                        pet.getId(),
                        pet.getNome(),
                        pet.getRaca().getDescricao(),
                        pet.getDataNascimento(),
                        pet.getCliente().getNome()))
                .collect(Collectors.toList());

        return new PageImpl<>(petResponseDtos, pageable, pets.size());
    }

    public PetResponseDto findById(Integer id) {
        Pet pet = repository.findById(id)
                .orElseThrow(() -> new PetshopException("Pet não encontrado com o ID: " + id));
        return new PetResponseDto(
                pet.getId(),
                pet.getNome(),
                pet.getRaca().getDescricao(),
                pet.getDataNascimento(),
                pet.getCliente().getNome());
    }

    @Transactional
    public PetResponseDto create(PetCreateDto petDto) {
        Cliente cliente = clienteRepository.findById(petDto.getClienteId())
                .orElseThrow(() -> new PetshopException("Erro", "Cliente não encontrado"));

        Optional<Raca> racaOptional = racaRepository.findByDescricao(petDto.getRacaDescricao());

        Raca raca = racaOptional.orElseGet(() -> {
            Raca novaRaca = new Raca();
            novaRaca.setDescricao(petDto.getRacaDescricao());
            return racaRepository.save(novaRaca);
        });

        Pet pet = new Pet();
        pet.setNome(petDto.getNome());
        pet.setDataNascimento(petDto.getDataNascimento());
        pet.setRaca(raca);
        pet.setCliente(cliente);

        pet = repository.save(pet);
        return new PetResponseDto(
                pet.getId(),
                pet.getNome(),
                pet.getRaca().getDescricao(),
                pet.getDataNascimento(),
                pet.getCliente().getNome());
    }

    @Transactional
    public PetResponseDto update(Integer id, PetUpdateDto updatedPet) {
        Usuario usuarioAutenticado = getUsuarioAutenticado();

        Pet existingPet = repository.findById(id)
                .orElseThrow(() -> new PetshopException("Erro", "Pet não encontrado"));

        if (!usuarioAutenticado.getPerfil().equals(Perfil.ADMIN)) {
            Cliente clienteDoUsuario = usuarioAutenticado.getCliente();
            if (clienteDoUsuario == null || !clienteDoUsuario.getId().equals(existingPet.getCliente().getId())) {
                throw new PetshopException("Permissão negada", "Você só pode editar seus próprios pets.");
            }
        }

        if(updatedPet.getNome() != null && !updatedPet.getNome().isBlank()) {
            existingPet.setNome(updatedPet.getNome());
        }

        if(updatedPet.getDataNascimento() != null) {
            existingPet.setDataNascimento(updatedPet.getDataNascimento());
        }

        if(updatedPet.getRacaDescricao() != null && !updatedPet.getRacaDescricao().isBlank()) {
            Optional<Raca> racaOptional = racaRepository.findByDescricao(updatedPet.getRacaDescricao());

            Raca raca = racaOptional.orElseGet(() -> {
                Raca novaRaca = new Raca();
                novaRaca.setDescricao(updatedPet.getRacaDescricao());
                return racaRepository.save(novaRaca);
            });

            existingPet.setRaca(raca);
        }

        existingPet = repository.save(existingPet);
        return new PetResponseDto(
                existingPet.getId(),
                existingPet.getNome(),
                existingPet.getRaca().getDescricao(),
                existingPet.getDataNascimento(),
                existingPet.getCliente().getNome());
    }

    @Transactional
    public void delete(Integer id) {
        Pet pet = repository.findById(id)
                .orElseThrow(() -> new PetshopException("Erro", "Pet não encontrado"));

        repository.delete(pet);
    }
}
