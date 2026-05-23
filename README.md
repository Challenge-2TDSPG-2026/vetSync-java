# JornadaPet 🐾

Aplicação backend para continuidade do cuidado e engajamento na jornada de saúde do pet. Desenvolvida em Spring Boot com API RESTful e autenticação JWT — Clyvo Vet Challenge 2026.

---

## Integrantes

| Nome | RM |
|---|---|
| Arthur Brito | RM 562085 |
| Luiz Felipe Flosi | RM 563197 |
| Pedro Brum | RM 561780 |

---

## 📁 Documentação

Os arquivos de suporte estão na pasta [`/documentos`](./documentos):

| Arquivo | Descrição |
|---|---|
| [Coleção Postman](./documentos/JornadaPet_Postman_Collection.json) | Todas as requisições para testar a API (importe no Postman) |
| [Diagrama de Classes](./documentos/diagrama-classes.png) | Entidades, repositórios, serviços e controllers |
| [DER](./documentos/der.png) | Diagrama Entidade-Relacionamento das tabelas |
| [Cronograma](./documentos/cronograma-sprint1.md) | Divisão de tarefas e prazos do Sprint 1 |
| [Cronograma (xlsx)](./documentos/cronograma.xlsx) | Versão em planilha do cronograma |

---

## Estrutura do Projeto

```
JornadaPet-java-Sprint1/
├── documentos/
│   ├── JornadaPet_Postman_Collection.json
│   ├── diagrama-classes.png
│   ├── der.png
│   ├── cronograma-sprint1.md
│   └── cronograma.xlsx
├── src/
│   └── main/
│       ├── java/br/com/fiap/JornadaPet/
│       │   ├── JornadaPetApplication.java
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   └── SwaggerConfig.java
│       │   ├── controller/
│       │   │   ├── AuthController.java
│       │   │   ├── TutorController.java
│       │   │   ├── PetController.java
│       │   │   └── EventoController.java
│       │   ├── entity/
│       │   │   ├── Tutor.java
│       │   │   ├── Pet.java
│       │   │   └── EventoSaude.java
│       │   ├── repository/
│       │   │   ├── TutorRepository.java
│       │   │   ├── PetRepository.java
│       │   │   └── EventoSaudeRepository.java
│       │   ├── service/
│       │   │   ├── TutorService.java
│       │   │   ├── PetService.java
│       │   │   ├── EventoService.java
│       │   │   └── JwtService.java
│       │   ├── security/
│       │   │   ├── JwtFilter.java
│       │   │   └── TutorUserDetailsService.java
│       │   └── data/
│       │       └── MockData.java
│       └── resources/
│           └── application.properties
└── pom.xml
```

---

## Tecnologias

- Java 17
- Spring Boot 3
- Spring Web
- Spring Security
- Spring Data JPA
- H2 Database (in-memory)
- JWT (jjwt)
- Lombok
- Springdoc OpenAPI (Swagger UI)

---

## Como Rodar

### Pré-requisitos

- Java 17+
- Maven 3.8+

### Executar

Na raiz do projeto:

```bash
mvn spring-boot:run
```

A API ficará disponível em `http://localhost:8080`

| Recurso | URL |
|---|---|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console |

**Configurações do H2:**
- JDBC URL: `jdbc:h2:mem:jornadapet`
- Usuário: `sa`
- Senha: *(vazio)*

---

## Autenticação

A API utiliza JWT (JSON Web Token). Siga os passos:

**1. Registre um tutor:**
```
POST /auth/register
```
```json
{
  "nome": "Carlos Teste",
  "email": "carlos@email.com",
  "senha": "senha123",
  "telefone": "11999998888",
  "cpf": "12345678901"
}
```

**2. Faça login:**
```
POST /auth/login
```
```json
{
  "email": "carlos@email.com",
  "senha": "senha123"
}
```

**3. Use o token retornado em todas as requisições protegidas:**
```
Authorization: Bearer <token>
```

> ⚠️ Os tutores do MockData não possuem senha. Para autenticar, cadastre um novo tutor via `POST /auth/register`.

---

## Endpoints da API

### Auth

| Método | Rota | Descrição |
|---|---|---|
| POST | `/auth/register` | Cadastrar tutor com senha |
| POST | `/auth/login` | Login — retorna JWT |

### Tutores

| Método | Rota | Descrição |
|---|---|---|
| GET | `/tutores` | Lista todos os tutores |
| GET | `/tutores/{id}` | Retorna um tutor pelo ID |
| POST | `/tutores` | Cadastrar tutor |
| PUT | `/tutores/{id}` | Atualizar tutor |
| DELETE | `/tutores/{id}` | Deletar tutor |

### Pets

| Método | Rota | Descrição |
|---|---|---|
| POST | `/pets/tutor/{tutorId}` | Cadastrar pet vinculado a um tutor |
| GET | `/pets/{id}` | Retorna um pet pelo ID (com idade calculada) |
| GET | `/pets/tutor/{tutorId}` | Lista pets por tutor (paginado) |
| GET | `/pets?especie=` | Lista pets por espécie |
| PUT | `/pets/{id}` | Atualizar pet |
| DELETE | `/pets/{id}` | Deletar pet |

### Eventos de Saúde

| Método | Rota | Descrição |
|---|---|---|
| POST | `/pets/{petId}/eventos` | Registrar evento de saúde |
| GET | `/pets/{petId}/eventos` | Lista eventos do pet (paginado) |
| GET | `/pets/{petId}/eventos/pendentes` | Lista eventos pendentes |
| GET | `/pets/{petId}/eventos/atrasados` | Lista eventos atrasados |
| GET | `/pets/{petId}/eventos/{id}` | Retorna um evento pelo ID |
| PATCH | `/pets/{petId}/eventos/{id}/realizado` | Marcar evento como realizado |
| PUT | `/pets/{petId}/eventos/{id}` | Atualizar evento |
| DELETE | `/pets/{petId}/eventos/{id}` | Deletar evento |

### Parâmetros de paginação

```
GET /pets/tutor/1?page=0&size=10&sort=nome,asc
GET /pets/1/eventos?page=0&size=10&sort=dataProxima,asc
```

---

## Exemplo de Resposta

**GET /pets/{id}**
```json
{
  "id": 1,
  "nome": "Buddy",
  "especie": "cão",
  "raca": "Golden Retriever",
  "peso": 5.0,
  "dataNascimento": "2024-02-15",
  "idadeAnos": 0,
  "sexo": "M",
  "castrado": false,
  "observacoes": "Filhote muito ativo",
  "tutorId": 1
}
```

---

## Modelo de Dados

**Tutor**
```
├── id         Long
├── nome       String
├── email      String (unique)
├── telefone   String
├── cpf        String
└── senha      String (BCrypt)
```

**Pet**
```
├── id              Long
├── nome            String
├── especie         String
├── raca            String
├── peso            Double
├── dataNascimento  LocalDate
├── sexo            String (M/F)
├── castrado        boolean
├── observacoes     String
└── tutor           Tutor (ManyToOne)
```

**EventoSaude**
```
├── id              Long
├── tipo            TipoEvento (VACINA, VERMIFUGO, BANHO, TOSA, CHECKUP, CONSULTA, CIRURGIA, MEDICAMENTO)
├── status          StatusEvento (PENDENTE, REALIZADO, ATRASADO)
├── descricao       String
├── dataRealizacao  LocalDate
├── dataProxima     LocalDate
└── pet             Pet (ManyToOne)
```

---

## Dados de Exemplo

A classe `MockData` é carregada automaticamente ao iniciar a aplicação e popula o banco com:

- 2 tutores (Maria Silva, João Souza)
- 3 pets (Buddy — Golden Retriever, Luna — Siamês, Rex — Pastor Alemão)
- 3 eventos de saúde (banho atrasado, consulta realizada, vacina pendente)
