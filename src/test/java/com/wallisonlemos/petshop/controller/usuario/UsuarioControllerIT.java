    package com.wallisonlemos.petshop.controller.usuario;

    import com.fasterxml.jackson.core.type.TypeReference;
    import com.fasterxml.jackson.databind.JsonNode;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
    import com.wallisonlemos.petshop.mock.TokenMock;
    import com.wallisonlemos.petshop.model.domain.cliente.Cliente;
    import com.wallisonlemos.petshop.model.domain.cliente.Contato;
    import com.wallisonlemos.petshop.model.domain.cliente.Endereco;
    import com.wallisonlemos.petshop.model.domain.cliente.enums.ContatoTipo;
    import com.wallisonlemos.petshop.model.domain.pet.Pet;
    import com.wallisonlemos.petshop.model.domain.pet.Raca;
    import com.wallisonlemos.petshop.model.domain.usuario.Usuario;
    import com.wallisonlemos.petshop.model.domain.usuario.enums.Perfil;
    import com.wallisonlemos.petshop.model.dto.auth.LoginDto;
    import com.wallisonlemos.petshop.model.dto.auth.LoginResponseDto;
    import com.wallisonlemos.petshop.model.dto.auth.UsuarioResponseDto;
    import com.wallisonlemos.petshop.model.dto.cliente.*;
    import com.wallisonlemos.petshop.repository.ClienteRepository;
    import com.wallisonlemos.petshop.repository.PetRepository;
    import com.wallisonlemos.petshop.repository.RacaRepository;
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

    import static org.junit.jupiter.api.Assertions.*;

    @SpringBootTest
    @AutoConfigureMockMvc
    @ClearDatabase
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class UsuarioControllerIT {

        @Autowired
        private MockMvc mockMvc;

        private static String adminToken;

        private static JsonTestUtil<ClienteCreateDto> jsonTestCadastro;
        private static JsonTestUtil<ClienteUpdateDto> jsonTestEdicao;
        private static JsonTestUtil<LoginDto> jsonTestAuth;

        private static Integer mariaId;
        private static String clienteToken;
        private static Integer jonasId;


        @BeforeAll
        static void setup(@Autowired TokenMock tokenMock, @Autowired UsuarioRepository usuarioRepository, @Autowired RacaRepository racaRepository, @Autowired PetRepository petRepository) throws Exception {
            jsonTestCadastro = new JsonTestUtil<>();
            jsonTestEdicao = new JsonTestUtil<>();
            jsonTestAuth = new JsonTestUtil<>();
            adminToken = tokenMock.getToken();
            criarClienteJonas(usuarioRepository, racaRepository, petRepository);
        }

        @Test
        @Order(1)
        @DisplayName("Não deve cadastrar um novo usuário sem permissão ADMIN")
        void naoDeveCadastrarUmNovoUsuarioSemPermissaoAdmin() throws Exception {
            EnderecoCreateDto enderecoCreateDto = new EnderecoCreateDto("Rua 13", "Serra Talhada", "Centro", "Lado A", "Residencial");
            ContatoCreateDto contatoCreateDto = new ContatoCreateDto("Pessoal", "TELEFONE", "(87)99999-9999");
            ClienteCreateDto clienteCreateDto = new ClienteCreateDto("07177804064", "Maria da Silva", "Maria1234",
                    "CLIENTE", enderecoCreateDto, contatoCreateDto);

            mockMvc.perform(MockMvcRequestBuilders.post("/auth/cadastrar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonTestCadastro.parseToJSONString(clienteCreateDto)))
                    .andExpect(MockMvcResultMatchers.status().isForbidden())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.erro").value("Não autorizado!"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.mensagem")
                            .value("Apenas administradores tem essa permissão."))
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        @Order(2)
        @DisplayName("Deve cadastrar um novo usuário")
        void deveCadastrarUmNovoUsuario() throws Exception {
            EnderecoCreateDto enderecoCreateDto = new EnderecoCreateDto("Rua 13", "Serra Talhada", "Centro", "Lado A", "Residencial");
            ContatoCreateDto contatoCreateDto = new ContatoCreateDto("Pessoal", "TELEFONE", "(87)99999-9999");
            ClienteCreateDto clienteCreateDto = new ClienteCreateDto("07177804064", "Maria da Silva", "Maria1234",
                    "CLIENTE", enderecoCreateDto, contatoCreateDto);

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/auth")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON).content(jsonTestCadastro.parseToJSONString(clienteCreateDto)))
            .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andDo(MockMvcResultHandlers.print())
                    .andReturn();

            String jsonResponse = result.getResponse().getContentAsString();

            ObjectMapper objectMapper = new ObjectMapper();
            UsuarioResponseDto usuario = objectMapper.readValue(jsonResponse, UsuarioResponseDto.class);

            assertEquals("07177804064", usuario.cpf());
            assertEquals("Maria da Silva", usuario.nome());
            assertEquals(Perfil.CLIENTE, usuario.perfil());
        }

        @Test
        @Order(3)
        @DisplayName("Deve fazer login")
        public void deveFazerLogin() throws Exception {
            LoginDto authDto = new LoginDto("07177804064", "Maria1234");
            JsonTestUtil<Object> jsonTestAuth = new JsonTestUtil<>();

            MvcResult result = mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content(jsonTestAuth.parseToJSONString(authDto)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andDo(MockMvcResultHandlers.print())
                    .andReturn();

            String jsonResponse = result.getResponse().getContentAsString();

            ObjectMapper objectMapper = new ObjectMapper();
            LoginResponseDto usuario = objectMapper.readValue(jsonResponse, LoginResponseDto.class);
            clienteToken = usuario.token();
            mariaId = usuario.id();

            assertEquals("07177804064", usuario.cpf());
            assertEquals("Maria da Silva", usuario.nome());
            assertNotNull(usuario.token());
        }

        @Test
        @Order(4)
        @DisplayName("Não deve fazer login com CPF inválido")
        public void naoDeveFazerLoginComCPFInvalido() throws Exception {
            LoginDto authDto = new LoginDto("07177804060", "Maria1234");
            JsonTestUtil<Object> jsonTestAuth = new JsonTestUtil<>();

            mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content(jsonTestAuth.parseToJSONString(authDto)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.erro").value("Não autorizado!"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.mensagem")
                            .value("Verifique o email ou senha e tente novamente!"))
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        @Order(5)
        @DisplayName("Não deve fazer login com senha inválida")
        public void naoDeveFazerLoginComSenhaInvalida() throws Exception {
            LoginDto authDto = new LoginDto("07177804064", "Maria1230");
            JsonTestUtil<Object> jsonTestAuth = new JsonTestUtil<>();

            mockMvc
                    .perform(MockMvcRequestBuilders.post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content(jsonTestAuth.parseToJSONString(authDto)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.erro").value("Não autorizado!"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.mensagem")
                            .value("Verifique o email ou senha e tente novamente!"))
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        @Order(6)
        @DisplayName("Não deve cadastrar um novo usuário com CPF inválido e senha menor que 8 dígitos")
        void naoDeveCadastrarUmNovoUsuarioComDadosInválidos() throws Exception {
            EnderecoCreateDto enderecoCreateDto = new EnderecoCreateDto("Rua 13", "Serra Talhada", "Centro", "Lado A", "Residencial");
            ContatoCreateDto contatoCreateDto = new ContatoCreateDto("Pessoal", "TELEFONE", "(87)99999-9999");
            ClienteCreateDto clienteCreateDto = new ClienteCreateDto("00000000000", "Maria da Silva", "1234",
                    "CLIENTE", enderecoCreateDto, contatoCreateDto);

            mockMvc.perform(MockMvcRequestBuilders.post("/auth")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonTestCadastro.parseToJSONString(clienteCreateDto)))
                    .andExpect(MockMvcResultMatchers
                            .content().json(
                                    "["
                                            + "{\"erro\":\"Erro\",\"mensagem\":\"O CPF informado não é válido\"},"
                                            + "{\"erro\":\"Erro\",\"mensagem\":\"A senha deve ter no mínimo 8 caracteres\"}"
                                            + "]"))
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        @Order(7)
        @DisplayName("Não deve cadastrar um novo usuário com email e senha vázios")
        void naoDeveCadastrarUmNovoUsuarioComDadosVazios() throws Exception {
            ClienteCreateDto cadastroDto = new ClienteCreateDto();

            mockMvc.perform(MockMvcRequestBuilders.post("/auth")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonTestCadastro.parseToJSONString(cadastroDto)))
                    .andExpect(MockMvcResultMatchers
                            .content().json(
                                    "["
                                            + "{\"erro\":\"Erro\",\"mensagem\":\"É necessário informar um contato\"},"
                                            + "{\"erro\":\"Erro\",\"mensagem\":\"É necessário informar um endereço\"},"
                                            + "{\"erro\":\"Erro\",\"mensagem\":\"É obrigatório informar uma senha\"},"
                                            + "{\"erro\":\"Erro\",\"mensagem\":\"É obrigatório informar um CPF válido\"},"
                                            + "{\"erro\":\"Erro\",\"mensagem\":\"É obrigatório informar um perfil de usuário\"},"
                                            + "{\"erro\":\"Erro\",\"mensagem\":\"É obrigatório informar o nome completo\"}"
                                            + "]"))
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        @Order(8)
        @DisplayName("Não deve cadastrar um novo usuário com cpf em uso")
        void naoDeveCadastrarUmNovoUsuarioComCpfEmUso() throws Exception {
            EnderecoCreateDto enderecoCreateDto = new EnderecoCreateDto("Rua 13", "Serra Talhada", "Centro", "Lado A", "Residencial");
            ContatoCreateDto contatoCreateDto = new ContatoCreateDto("Pessoal", "TELEFONE", "(87)99999-9999");
            ClienteCreateDto clienteCreateDto = new ClienteCreateDto("06777953000", "Maria da Silva", "Maria1234",
                    "CLIENTE", enderecoCreateDto, contatoCreateDto);

            mockMvc.perform(MockMvcRequestBuilders.post("/auth")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonTestCadastro.parseToJSONString(clienteCreateDto)))
                    .andExpect(MockMvcResultMatchers
                            .content()
                            .json("{\"erro\":\"Erro\",\"mensagem\":\"O CPF 06777953000 já está em uso.\"}"))
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        @Order(9)
        @DisplayName("Deve editar um usuário como administrador")
        void deveEditarUsuarioComoAdmin() throws Exception {
            EnderecoCreateDto novoEndereco = new EnderecoCreateDto(
                    "Rua Atualizada",
                    "Nova Cidade",
                    "Novo Bairro",
                    "S/N",
                    "Trabalho"
            );

            ContatoCreateDto novoContato = new ContatoCreateDto(
                    "Profissional",
                    "EMAIL",
                    "nova.maria@example.com"
            );

            ClienteUpdateDto edicaoDto = new ClienteUpdateDto();
            edicaoDto.setNome("Maria Silva Atualizada");
            edicaoDto.setEndereco(novoEndereco);
            edicaoDto.setContato(novoContato);

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/auth/{id}", mariaId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonTestEdicao.parseToJSONString(edicaoDto)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andDo(MockMvcResultHandlers.print())
                    .andReturn();

            String jsonResponse = result.getResponse().getContentAsString();
            UsuarioResponseDto response = new ObjectMapper().readValue(jsonResponse, UsuarioResponseDto.class);

            assertEquals("Maria Silva Atualizada", response.nome());
            assertEquals("07177804064", response.cpf());
        }

        @Test
        @Order(10)
        @DisplayName("Deve editar o próprio usuário como cliente")
        void deveEditarProprioUsuarioComoCliente() throws Exception {
            ClienteUpdateDto edicaoDto = new ClienteUpdateDto();
            ContatoCreateDto novoContato = new ContatoCreateDto(
                    "Celular",
                    "TELEFONE",
                    "(11)98765-4321"
            );
            edicaoDto.setContato(novoContato);

            mockMvc.perform(MockMvcRequestBuilders.put("/auth/{id}", mariaId)
                            .header("Authorization", "Bearer " + clienteToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonTestEdicao.parseToJSONString(edicaoDto)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.nome").value("Maria Silva Atualizada"))
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        @Order(11)
        @DisplayName("Não deve editar outro usuário sem ser admin")
        void naoDeveEditarOutroUsuarioSemSerAdmin() throws Exception {
            ClienteUpdateDto edicaoDto = new ClienteUpdateDto();
            edicaoDto.setNome("Nome Maria Alterado");

            mockMvc.perform(MockMvcRequestBuilders.put("/auth/{id}", jonasId)
                            .header("Authorization", "Bearer " + clienteToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonTestEdicao.parseToJSONString(edicaoDto)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.erro").value("Permissão negada"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.mensagem").value("Você só pode editar seus próprios dados."))
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        @Order(12)
        @DisplayName("Não deve editar perfil para admin como cliente")
        void naoDeveEditarPerfilParaAdminComoCliente() throws Exception {
            ClienteUpdateDto edicaoDto = new ClienteUpdateDto();
            edicaoDto.setPerfil("ADMIN");

            mockMvc.perform(MockMvcRequestBuilders.put("/auth/{id}", mariaId)
                            .header("Authorization", "Bearer " + clienteToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonTestEdicao.parseToJSONString(edicaoDto)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.erro").value("Erro"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.mensagem").value("Apenas administradores podem alterar perfis de usuário!"))
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        @Order(13)
        @DisplayName("Não deve editar com email inválido")
        void naoDeveEditarComEmailInvalido() throws Exception {
            ContatoCreateDto contato = new ContatoCreateDto("Email", "EMAIL", "email-invalido");
            ClienteUpdateDto edicaoDto = new ClienteUpdateDto();
            edicaoDto.setContato(contato);

            mockMvc.perform(MockMvcRequestBuilders.put("/auth/{id}", mariaId)
                            .header("Authorization", "Bearer " + clienteToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonTestEdicao.parseToJSONString(edicaoDto)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.erro").value("Erro"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.mensagem").value("Formato de email inválido"))
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        @Order(14)
        @DisplayName("Deve exibir o cliente para usuário id informado")
        void deveListarExibirOClienteParaOIDInformado() throws Exception {
            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/auth/clientes/{id}", jonasId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andDo(MockMvcResultHandlers.print())
                    .andReturn();

            String jsonResponse = result.getResponse().getContentAsString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            ClienteDto cliente = objectMapper.readValue(jsonResponse, new TypeReference<>() {});

            assertEquals("Jonas Silva", cliente.nome());
            assertEquals(1, cliente.pets().size());
            assertEquals("Piaba", cliente.pets().get(0).nome());
        }

        @Test
        @Order(15)
        @DisplayName("Deve listar todos os clientes para usuário ADMIN")
        void deveListarTodosOsClientesParaUsuarioAdmin() throws Exception {
            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/auth/clientes")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andDo(MockMvcResultHandlers.print())
                    .andReturn();

            String jsonResponse = result.getResponse().getContentAsString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode contentNode = rootNode.get("content");

            List<ClienteResponseDto> clientes = objectMapper.readValue(contentNode.toString(), new TypeReference<>() {});

            assertEquals(3, clientes.size(), "A quantidade de clientes retornados não é a esperada.");
        }

        @Test
        @Order(16)
        @DisplayName("CLIENTE deve listar apenas seus próprio perfil de cliente")
        void clienteDeveListarApenasSeusProprioPerfilDeCliente() throws Exception {
            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/auth/clientes")
                            .header("Authorization", "Bearer " + clienteToken))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andDo(MockMvcResultHandlers.print())
                    .andReturn();

            String jsonResponse = result.getResponse().getContentAsString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode contentNode = rootNode.get("content");

            List<ClienteResponseDto> clientes = objectMapper.readValue(contentNode.toString(), new TypeReference<>() {});

            assertEquals(1, clientes.size(), "A quantidade de clientes retornados não é a esperada.");
        }

        @Test
        @Order(17)
        @DisplayName("Deve deletar um cliente")
        void deveDeletarUmCliente(@Autowired ClienteRepository repository) throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.delete("/auth/{id}", jonasId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(MockMvcResultMatchers.status().isNoContent())
                    .andDo(MockMvcResultHandlers.print());

            Optional<Cliente> cliente = repository.findById(jonasId);

            assertFalse(cliente.isPresent(), "O cliente não foi deletado como deveria.");
        }

        @Test
        @Order(18)
        @DisplayName("Não deve deletar um cliente sem ser ADMIN")
        void NaoDeveDeletarUmClienteSemSerAdmin() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.delete("/auth/{id}", mariaId)
                            .header("Authorization", "Bearer " + clienteToken))
                    .andExpect(MockMvcResultMatchers.status().isForbidden())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.erro").value("Não autorizado!"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.mensagem")
                            .value("Apenas administradores tem essa permissão."))
                    .andDo(MockMvcResultHandlers.print());
        }

        private static void criarClienteJonas(UsuarioRepository usuarioRepository, RacaRepository racaRepository, PetRepository petRepository) {
            String senhaEncriptada = new BCryptPasswordEncoder().encode("Pet0123456");

            Usuario usuario = new Usuario("03238619039", "Jonas Silva", senhaEncriptada, Perfil.ADMIN);
            Cliente cliente = new Cliente(usuario);
            cliente.setEndereco(new Endereco("Rua central", "Serra Talhada", "Centro", "Lado A", "Residencia", cliente));
            cliente.setContato(new Contato("Pessoal", ContatoTipo.TELEFONE, "(87)98111-1111", cliente));
            usuario.setCliente(cliente);

            usuario = usuarioRepository.save(usuario);

            jonasId = usuario.getId();
            Raca raca = new Raca();
            raca.setDescricao("SRD");
            racaRepository.save(raca);

            Pet pet = new Pet("Piaba", LocalDate.of(2024, 12, 12), cliente, raca);
            raca.setPets(List.of(pet));

            petRepository.save(pet);
        }
    }