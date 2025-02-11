package com.wallisonlemos.petshop.controller.pet;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wallisonlemos.petshop.model.dto.pet.PetResponseDto;
import com.wallisonlemos.petshop.mock.TokenMock;
import com.wallisonlemos.petshop.model.domain.cliente.Cliente;
import com.wallisonlemos.petshop.model.domain.cliente.Contato;
import com.wallisonlemos.petshop.model.domain.cliente.Endereco;
import com.wallisonlemos.petshop.model.domain.cliente.enums.ContatoTipo;
import com.wallisonlemos.petshop.model.domain.pet.Pet;
import com.wallisonlemos.petshop.model.domain.usuario.Usuario;
import com.wallisonlemos.petshop.model.domain.usuario.enums.Perfil;
import com.wallisonlemos.petshop.model.dto.auth.LoginDto;
import com.wallisonlemos.petshop.model.dto.auth.LoginResponseDto;
import com.wallisonlemos.petshop.model.dto.pet.PetCreateDto;
import com.wallisonlemos.petshop.model.dto.pet.PetUpdateDto;
import com.wallisonlemos.petshop.repository.PetRepository;
import com.wallisonlemos.petshop.repository.UsuarioRepository;
import com.wallisonlemos.petshop.utils.db.ClearDatabase;
import com.wallisonlemos.petshop.utils.test.JsonTestUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@AutoConfigureMockMvc
@ClearDatabase
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PetControllerIT {


    private static MockMvc mockMvc;
    private static String adminToken;
    private static JsonTestUtil<PetCreateDto> jsonTestCreate;
    private static JsonTestUtil<PetUpdateDto> jsonTestUpdate;
    private static JsonTestUtil<LoginDto> jsonTestAuth;
    private static Integer mariaId;
    private static String clienteToken;
    private static Integer joaoId;
    private static Integer petId;
    private static Integer joaoPetId;


    @BeforeAll
    static void setup(@Autowired MockMvc injectedMockMvc, @Autowired TokenMock tokenMock, @Autowired UsuarioRepository usuarioRepository) throws Exception {
        mockMvc = injectedMockMvc;
        jsonTestCreate = new JsonTestUtil<>();
        jsonTestUpdate = new JsonTestUtil<>();
        jsonTestAuth = new JsonTestUtil<>();
        adminToken = tokenMock.getToken();

        mariaId = criarClienteMaria(usuarioRepository);
        clienteToken = logarComMaria();

        joaoId = criarClienteJoao(usuarioRepository);
        criarPetDoJoao();
        joaoPetId = 1;
    }

    @Test
    @Order(1)
    @DisplayName("Não deve cadastrar um novo pet sem permissão ADMIN")
    void naoDeveCadastrarUmNovoPetSemPermissaoAdmin() throws Exception {
        PetCreateDto petCreateDto = new PetCreateDto(
                "Itachi",
                LocalDate.of(2025, 2, 4),
                "Akita Inu",
                mariaId);

        mockMvc.perform(MockMvcRequestBuilders.post("/pets")
                        .header("Authorization", "Bearer " + clienteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTestCreate.parseToJSONString(petCreateDto)))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.erro").value("Não autorizado!"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mensagem")
                        .value("Apenas administradores tem essa permissão."))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(2)
    @DisplayName("Deve cadastrar um novo pet")
    void deveCadastrarUmNovoPet() throws Exception {
        PetCreateDto petCreateDto = new PetCreateDto(
                "Itachi",
                LocalDate.of(2025, 2, 4),
                "Akita Inu",
                mariaId);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/pets")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTestCreate.parseToJSONString(petCreateDto)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        PetResponseDto pet = objectMapper.readValue(jsonResponse, PetResponseDto.class);

        assertEquals("Itachi", pet.nome());
        assertEquals("Maria da Silva", pet.cliente());
        assertEquals(LocalDate.of(2025, 2, 4), pet.dataNascimento());

        petId = pet.id();
    }

    @Test
    @Order(3)
    @DisplayName("Não deve cadastrar um novo pet com dados inválidos")
    void naoDeveCadastrarUmNovoPetComDadosInvalidos() throws Exception {
        PetCreateDto petCreateDto = new PetCreateDto();

        mockMvc.perform(MockMvcRequestBuilders.post("/pets")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTestCreate.parseToJSONString(petCreateDto)))
                .andExpect(MockMvcResultMatchers
                        .content().json(
                        "["
                                + "{\"erro\":\"Erro\",\"mensagem\":\"Obrigatório informar o cliente\"},"
                                + "{\"erro\":\"Erro\",\"mensagem\":\"É obrigatório descrever a raça\"},"
                                + "{\"erro\":\"Erro\",\"mensagem\":\"É obrigatório informar o nome do pet\"}"
                                + "]"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(4)
    @DisplayName("Admin pode atualizar qualquer pet")
    void adminPodeAtualizarPet() throws Exception {
        PetUpdateDto updateDto = new PetUpdateDto();
        updateDto.setNome("Itachi Uchiha");

        mockMvc.perform(MockMvcRequestBuilders.put("/pets/{id}", petId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTestUpdate.parseToJSONString(updateDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.nome").value("Itachi Uchiha"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(5)
    @DisplayName("Cliente pode atualizar seu próprio pet")
    void clientePodeAtualizarProprioPet() throws Exception {
        PetUpdateDto updateDto = new PetUpdateDto();
        updateDto.setDataNascimento(LocalDate.of(2020, 1, 1));

        mockMvc.perform(MockMvcRequestBuilders.put("/pets/{id}", petId)
                        .header("Authorization", "Bearer " + clienteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTestUpdate.parseToJSONString(updateDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.dataNascimento").value("2020-01-01"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(6)
    @DisplayName("Cliente não pode atualizar pet de outro cliente")
    void clienteNaoPodeAtualizarPetDeOutroCliente() throws Exception {
        PetUpdateDto updateDto = new PetUpdateDto();
        updateDto.setNome("Novo Nome");

        mockMvc.perform(MockMvcRequestBuilders.put("/pets/{id}", joaoPetId)
                        .header("Authorization", "Bearer " + clienteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTestUpdate.parseToJSONString(updateDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.erro").value("Permissão negada"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mensagem").value("Você só pode editar seus próprios pets."))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(7)
    @DisplayName("Atualizar pet inexistente retorna erro")
    void atualizarPetInexistenteRetornaErro() throws Exception {
        PetUpdateDto updateDto = new PetUpdateDto();
        updateDto.setNome("Novo Nome");

        mockMvc.perform(MockMvcRequestBuilders.put("/pets/9999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTestUpdate.parseToJSONString(updateDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.erro").value("Erro"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mensagem").value("Pet não encontrado"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(8)
    @DisplayName("Deve atualizar a raça do pet")
    void deveAtualizarRacaDoPet() throws Exception {
        PetUpdateDto updateDto = new PetUpdateDto();
        updateDto.setRacaDescricao("Akita Tigrado");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/pets/{id}", petId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTestUpdate.parseToJSONString(updateDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        PetResponseDto pet = objectMapper.readValue(jsonResponse, PetResponseDto.class);

        assertEquals("Akita Tigrado", pet.raca());
    }

    @Test
    @Order(9)
    @DisplayName("Deve listar todos os pets para usuário ADMIN")
    void deveListarTodosOsPetsParaUsuarioAdmin() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/pets")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode contentNode = rootNode.get("content");

        List<PetResponseDto> pets = objectMapper.readValue(contentNode.toString(), new TypeReference<>() {});

        assertEquals(2, pets.size(), "A quantidade de pet retornadas não é a esperada.");
    }

    @Test
    @Order(10)
    @DisplayName("CLIENTE deve listar apenas seus próprios pets")
    void clienteDeveListarApenasSeusPets() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/pets")
                        .header("Authorization", "Bearer " + clienteToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode contentNode = rootNode.get("content");
        List<PetResponseDto> pets = objectMapper.readValue(contentNode.toString(), new TypeReference<>() {});

        assertEquals(1, pets.size(), "A quantidade de pet retornadas não é a esperada.");
    }

    @Test
    @Order(11)
    @DisplayName("Não deve deletar um pet sem ser ADMIN")
    void NaoDeveDeletarUmPetSemSerAdmin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/pets/{id}", petId)
                        .header("Authorization", "Bearer " + clienteToken))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.erro").value("Não autorizado!"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mensagem")
                        .value("Apenas administradores tem essa permissão."))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(12)
    @DisplayName("Deve deletar qualquer Pet com usuário ADMIN")
    void deveDeletarQualquerPet(@Autowired PetRepository repository) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/pets/{id}", petId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());

        Optional<Pet> pet = repository.findById(petId);

        assertFalse(pet.isPresent(), "O Pet não foi deletado como deveria.");
    }
    private static Integer criarClienteMaria(UsuarioRepository usuarioRepository) {
        String senhaEncriptada = new BCryptPasswordEncoder().encode("Maria1234");

        Usuario mariaDaSilva = new Usuario("07177804064", "Maria da Silva", senhaEncriptada, Perfil.CLIENTE);
        Cliente cliente = new Cliente(mariaDaSilva);
        cliente.setEndereco(new Endereco("Rua central", "Serra Talhada", "Centro", "Lado A", "Residencia", cliente));
        cliente.setContato(new Contato("Pessoal", ContatoTipo.TELEFONE, "(87)98111-1111", cliente));
        mariaDaSilva.setCliente(cliente);

        mariaDaSilva = usuarioRepository.save(mariaDaSilva);
        return mariaDaSilva.getId();
    }

    private static Integer criarClienteJoao(UsuarioRepository usuarioRepository) {
        String senhaEncriptada = new BCryptPasswordEncoder().encode("Joao1234");
        Usuario joao = new Usuario("53169233084", "Joao da Silva", senhaEncriptada, Perfil.CLIENTE);
        Cliente cliente = new Cliente(joao);
        cliente.setEndereco(new Endereco("Rua lateral", "Cidade", "Bairro", "Complemento", "Tipo", cliente));
        cliente.setContato(new Contato("Pessoal", ContatoTipo.TELEFONE, "(00)00000-0000", cliente));
        joao.setCliente(cliente);
        return usuarioRepository.save(joao).getId();
    }

    private static void criarPetDoJoao() throws Exception {
        PetCreateDto joaoPetDto = new PetCreateDto("Rex", LocalDate.now(), "Pastor Alemão", joaoId);
        mockMvc.perform(MockMvcRequestBuilders.post("/pets")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTestCreate.parseToJSONString(joaoPetDto)))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    private static String logarComMaria() throws Exception {
        LoginDto authDto = new LoginDto("07177804064", "Maria1234");

        MvcResult loginResult = mockMvc
                .perform(MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTestAuth.parseToJSONString(authDto)))
                .andReturn();

        String jsonLogin = loginResult.getResponse().getContentAsString();
        LoginResponseDto loginResponse = new ObjectMapper().readValue(jsonLogin, LoginResponseDto.class);
        return loginResponse.token();
    }

}
