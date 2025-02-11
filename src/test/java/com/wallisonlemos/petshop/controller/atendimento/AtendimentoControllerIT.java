package com.wallisonlemos.petshop.controller.atendimento;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wallisonlemos.petshop.mock.TokenMock;
import com.wallisonlemos.petshop.model.domain.cliente.Cliente;
import com.wallisonlemos.petshop.model.domain.cliente.Contato;
import com.wallisonlemos.petshop.model.domain.cliente.Endereco;
import com.wallisonlemos.petshop.model.domain.cliente.enums.ContatoTipo;
import com.wallisonlemos.petshop.model.domain.pet.Pet;
import com.wallisonlemos.petshop.model.domain.usuario.Usuario;
import com.wallisonlemos.petshop.model.domain.usuario.enums.Perfil;
import com.wallisonlemos.petshop.model.dto.atendimento.AtendimentoCreateDto;
import com.wallisonlemos.petshop.model.dto.atendimento.AtendimentoResponseDto;
import com.wallisonlemos.petshop.model.dto.atendimento.AtendimentoUpdateDto;
import com.wallisonlemos.petshop.model.dto.auth.LoginDto;
import com.wallisonlemos.petshop.model.dto.auth.LoginResponseDto;
import com.wallisonlemos.petshop.model.dto.pet.PetCreateDto;
import com.wallisonlemos.petshop.model.dto.pet.PetResponseDto;
import com.wallisonlemos.petshop.repository.PetRepository;
import com.wallisonlemos.petshop.repository.UsuarioRepository;
import com.wallisonlemos.petshop.utils.db.ClearDatabase;
import com.wallisonlemos.petshop.utils.test.JsonTestUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@AutoConfigureMockMvc
@ClearDatabase
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AtendimentoControllerIT {

    private static MockMvc mockMvc;
    private static String adminToken;
    private static JsonTestUtil<AtendimentoCreateDto> jsonTestCreate;
    private static JsonTestUtil<AtendimentoUpdateDto> jsonTestUpdate;
    private static JsonTestUtil<PetCreateDto> jsonTestPetCreate;
    private static JsonTestUtil<LoginDto> jsonTestAuth;
    private static Integer atendimentoId;
    private static Integer mariaId;
    private static String clienteToken;
    private static final Integer mariaPetId = 1;


    @BeforeAll
    static void setup(@Autowired MockMvc injectedMockMvc, @Autowired TokenMock tokenMock, @Autowired UsuarioRepository usuarioRepository) throws Exception {
        mockMvc = injectedMockMvc;
        jsonTestCreate = new JsonTestUtil<>();
        jsonTestUpdate = new JsonTestUtil<>();
        jsonTestPetCreate = new JsonTestUtil<>();
        jsonTestAuth = new JsonTestUtil<>();
        adminToken = tokenMock.getToken();

        mariaId = criarClienteMaria(usuarioRepository);
        clienteToken = logarComMaria();
        criarPetDaMaria();
    }

    @Test
    @Order(1)
    @DisplayName("Não deve cadastrar um novo atendimento sem permissão ADMIN")
    void naoDeveCadastrarUmNovoAtendimentoSemPermissaoAdmin() throws Exception {
        AtendimentoCreateDto atendimentoCreateDto = new AtendimentoCreateDto(
                mariaPetId,
                "Vacina contra raiva",
                BigDecimal.valueOf(160.00));

        mockMvc.perform(MockMvcRequestBuilders.post("/atendimentos")
                        .header("Authorization", "Bearer " + clienteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTestCreate.parseToJSONString(atendimentoCreateDto)))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.erro").value("Não autorizado!"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mensagem")
                        .value("Apenas administradores tem essa permissão."))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(2)
    @DisplayName("Deve cadastrar um novo atendimento")
    void deveCadastrarUmNovoAtendimento() throws Exception {
        AtendimentoCreateDto atendimentoCreateDto = new AtendimentoCreateDto(
                mariaPetId,
                "Vacina contra raiva",
                BigDecimal.valueOf(160.00));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/atendimentos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTestCreate.parseToJSONString(atendimentoCreateDto)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        AtendimentoResponseDto responseDto = objectMapper.readValue(jsonResponse, AtendimentoResponseDto.class);

        assertEquals("Rex", responseDto.petNome());
        assertEquals("Vacina contra raiva", responseDto.descricao());
        assertEquals(LocalDate.now(), responseDto.data());
        assertEquals(new BigDecimal("160.00"), responseDto.valor());

        atendimentoId = responseDto.id();
    }

    @Test
    @Order(3)
    @DisplayName("Deve exibir um atendimento por id")
    void deveExibirUmAtendimentoPorId() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/atendimentos/{id}", atendimentoId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        AtendimentoResponseDto responseDto = objectMapper.readValue(jsonResponse, AtendimentoResponseDto.class);

        assertEquals("Rex", responseDto.petNome());
        assertEquals("Vacina contra raiva", responseDto.descricao());
        assertEquals(LocalDate.now(), responseDto.data());
        assertEquals(new BigDecimal("160.00"), responseDto.valor());
    }

    @Test
    @Order(4)
    @DisplayName("Não deve exibir um atendimento por id inexistente")
    void naoDeveExibirUmAtendimentoPorIdInexistente() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/atendimentos/{id}", 999)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.erro").value("Erro"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mensagem")
                        .value("Atendimento não encontrado com o ID: 999"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(5)
    @DisplayName("Não deve cadastrar um novo atendimento com dados inválidos")
    void naoDeveCadastrarUmNovoPetComDadosInvalidos() throws Exception {
        AtendimentoCreateDto createDto = new AtendimentoCreateDto();

        mockMvc.perform(MockMvcRequestBuilders.post("/atendimentos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTestCreate.parseToJSONString(createDto)))
                .andExpect(MockMvcResultMatchers
                        .content().json(
                        "["
                                + "{\"erro\":\"Erro\",\"mensagem\":\"É obrigatório informar a descrição do atendimento\"},"
                                + "{\"erro\":\"Erro\",\"mensagem\":\"É obrigatório informar um valor para o atendimento\"},"
                                + "{\"erro\":\"Erro\",\"mensagem\":\"É obrigatório informar um Pet\"}"
                                + "]"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(6)
    @DisplayName("Admin pode atualizar qualquer atendimento")
    void adminPodeAtualizarAtendimento() throws Exception {
        AtendimentoUpdateDto updateDto = new AtendimentoUpdateDto();
        updateDto.setDescricao("Atendimento atualizado");
        updateDto.setValor(BigDecimal.valueOf(200));

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/atendimentos/{id}", atendimentoId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTestUpdate.parseToJSONString(updateDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        AtendimentoResponseDto responseDto = objectMapper.readValue(jsonResponse, AtendimentoResponseDto.class);

        assertEquals("Rex", responseDto.petNome());
        assertEquals("Atendimento atualizado", responseDto.descricao());
        assertEquals(LocalDate.now(), responseDto.data());
        assertEquals(new BigDecimal("200.00"), responseDto.valor());
    }

    @Test
    @Order(7)
    @DisplayName("Cliente não pode atualizar atendimento")
    void clienteNaoPodeAtualizarAtendimento() throws Exception {
        AtendimentoUpdateDto updateDto = new AtendimentoUpdateDto();
        updateDto.setDescricao("Teste update");

        mockMvc.perform(MockMvcRequestBuilders.put("/atendimentos/{id}", atendimentoId)
                        .header("Authorization", "Bearer " + clienteToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTestUpdate.parseToJSONString(updateDto)))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.erro").value("Não autorizado!"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mensagem")
                        .value("Apenas administradores tem essa permissão."))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(8)
    @DisplayName("Atualizar atendimento inexistente retorna erro")
    void atualizarPetInexistenteRetornaErro() throws Exception {
        AtendimentoUpdateDto updateDto = new AtendimentoUpdateDto();
        updateDto.setDescricao("Nova descrição");

        mockMvc.perform(MockMvcRequestBuilders.put("/atendimentos/9999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTestUpdate.parseToJSONString(updateDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.erro").value("Erro"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mensagem").value("Atendimento não encontrado"))
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    @Order(9)
    @DisplayName("Deve listar todos os atendimentos para usuário ADMIN")
    void deveListarTodosOsAtendimentosParaUsuarioAdmin() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/atendimentos")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode contentNode = rootNode.get("content");

        List<AtendimentoResponseDto> atendimentos = objectMapper.readValue(contentNode.toString(), new TypeReference<>() {});

        assertEquals(1, atendimentos.size(), "A quantidade de atendimentos retornados não é a esperada.");
    }

    @Test
    @Order(10)
    @DisplayName("CLIENTE deve listar apenas seus próprios atendimentos")
    void clienteDeveListarApenasSeusPets() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/atendimentos")
                        .header("Authorization", "Bearer " + clienteToken))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode contentNode = rootNode.get("content");

        List<AtendimentoResponseDto> atendimentos = objectMapper.readValue(contentNode.toString(), new TypeReference<>() {});

        assertEquals(1, atendimentos.size(), "A quantidade de atendimentos retornados não é a esperada.");
    }

    @Test
    @Order(11)
    @DisplayName("Não deve deletar um atendimento sem ser ADMIN")
    void NaoDeveDeletarUmPetSemSerAdmin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/atendimentos/{id}", atendimentoId)
                        .header("Authorization", "Bearer " + clienteToken))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.erro").value("Não autorizado!"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.mensagem")
                        .value("Apenas administradores tem essa permissão."))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @Order(12)
    @DisplayName("Deve deletar qualquer Atendimento com usuário ADMIN")
    void deveDeletarQualquerPet(@Autowired PetRepository repository) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/pets/{id}", atendimentoId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(MockMvcResultHandlers.print());

        Optional<Pet> pet = repository.findById(atendimentoId);

        assertFalse(pet.isPresent(), "O atendimento não foi deletado como deveria.");
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

    private static void criarPetDaMaria() throws Exception {
        PetCreateDto mariaPetDto = new PetCreateDto("Rex", LocalDate.now(), "Pastor Alemão", mariaId);
        mockMvc.perform(MockMvcRequestBuilders.post("/pets")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTestPetCreate.parseToJSONString(mariaPetDto)))
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
