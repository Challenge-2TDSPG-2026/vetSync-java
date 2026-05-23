# 🐾 JornadaPet

> **API REST para continuidade do cuidado e engajamento na jornada de saúde do pet**  
> FIAP Challenge 2026 — Parceria com **Clyvo Vet** | Java Advanced | 2º Ano ADS

---

## 👥 Integrantes

| Nome | RM |
|---|---|
| Arthur Brito | RM 562085 |
| Luiz Felipe Flosi | RM 563197 |
| Pedro Brum | RM 561780 |

---

## 📋 Descrição do Projeto

O **JornadaPet** é uma API backend desenvolvida em **Spring Boot** que resolve um problema real identificado em parceria com a **Clyvo Vet**: tutores de pets frequentemente esquecem ou negligenciam eventos preventivos de saúde — vacinas, vermifugações, consultas e banhos — o que resulta em visitas de emergência evitáveis e agravamento de condições tratáveis.

A aplicação oferece:

- **Cadastro e gestão de tutores e seus pets** com perfil completo (espécie, raça, peso, idade calculada automaticamente)
- **Jornada contínua de saúde**: registro e acompanhamento de eventos (vacinas, vermifugações, banhos, tosas, check-ups, consultas, cirurgias, medicamentos)
- **Alertas automáticos de status**: eventos atrasados são detectados e sinalizados automaticamente ao consultar
- **Sugestão de eventos iniciais** ao cadastrar um novo pet, baseada na faixa etária (filhote vs. adulto)
- **Autenticação JWT** para acesso seguro aos dados
- **Frontend integrado** (API Tester) servido pelo próprio Spring Boot para facilitar testes

---

## 💼 Benefícios para o Negócio

| Benefício | Impacto |
|---|---|
| Redução de emergências veterinárias | Tutores recebem alertas de eventos pendentes/atrasados, antecipando cuidados |
| Fidelização do cliente | A plataforma cria um vínculo contínuo entre tutor, pet e clínica |
| Histórico clínico centralizado | Todos os eventos de saúde do pet ficam registrados e consultáveis |
| Escalabilidade da solução | Arquitetura REST + Oracle suporta crescimento da base de dados |
| Redução de churn | Engajamento preventivo aumenta a frequência de visitas planejadas |
| Diferencial competitivo | Clyvo Vet entra no mercado brasileiro com solução digital de acompanhamento |

---

## 🏗️ Desenho Macro da Arquitetura

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENTE / FRONTEND                        │
│         (Postman · API Tester embutido · Apps futuros)          │
└──────────────────────────┬──────────────────────────────────────┘
                           │ HTTP/REST (JSON)
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT APPLICATION                       │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                   Security Layer (JWT)                   │   │
│  │         JwtFilter → TutorUserDetailsService              │   │
│  └──────────────────────┬──────────────────────────────────┘   │
│                          │                                       │
│  ┌───────────┐  ┌────────┴───────┐  ┌────────────────────────┐ │
│  │AuthControl│  │TutorController │  │  PetController         │ │
│  │  /auth/** │  │  /tutores/**   │  │  /pets/**              │ │
│  └─────┬─────┘  └───────┬────────┘  └──────────┬─────────────┘ │
│        │                │                        │               │
│        │         ┌──────▼──────┐        ┌───────▼─────────────┐│
│        │         │TutorService │        │PetService           ││
│        │         │@Cacheable   │        │EventoService        ││
│        │         └──────┬──────┘        └───────┬─────────────┘│
│        │                │                        │               │
│  ┌─────▼────────────────▼────────────────────────▼───────────┐ │
│  │               Spring Data JPA / Repositories               │ │
│  │    TutorRepository · PetRepository · EventoSaudeRepository │ │
│  └────────────────────────────┬───────────────────────────────┘ │
└───────────────────────────────┼─────────────────────────────────┘
                                │ JDBC (ojdbc11)
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    ORACLE XE 21c (Docker)                        │
│          Schema: APP_USER · DB: XEPDB1 · Port: 1521             │
└─────────────────────────────────────────────────────────────────┘
```

### Camadas da aplicação

| Camada | Responsabilidade |
|---|---|
| **Controller** | Recebe requisições HTTP, valida entrada com Bean Validation, delega ao Service, retorna DTOs (Records) |
| **Service** | Regras de negócio: cálculo de status, sugestão de eventos iniciais, cache, orquestração |
| **Repository** | Acesso a dados via Spring Data JPA — queries por espécie, status, tutor |
| **Security** | Filtro JWT stateless, BCrypt para senhas, Spring Security |
| **Entity** | Tutor, Pet, EventoSaude com mapeamento JPA e validações |

### Modelo de domínio

```
Tutor (1) ──────< Pet (N) ──────< EventoSaude (N)
  id                id                  id
  nome              nome                tipo (enum)
  email (unique)    especie             status (enum)
  cpf               raca                descricao
  telefone          peso                dataRealizacao
  senha (BCrypt)    dataNascimento      dataProxima
                    sexo
                    castrado
                    observacoes
```

---

## 🗂️ Estrutura do Projeto

```
JornadaPet/
├── documentos/
│   ├── JornadaPet_Postman_Collection.json   ← Importar no Postman
│   ├── diagrama-classes.png
│   ├── der.png
│   ├── cronograma-sprint1.md
│   └── cronograma.xlsx
├── src/
│   └── main/
│       ├── java/br/com/fiap/JornadaPet/
│       │   ├── config/         SecurityConfig, SwaggerConfig
│       │   ├── controller/     Auth, Tutor, Pet, Evento
│       │   ├── entity/         Tutor, Pet, EventoSaude
│       │   ├── repository/     Tutor, Pet, EventoSaude
│       │   ├── service/        Tutor, Pet, Evento, Jwt
│       │   ├── security/       JwtFilter, TutorUserDetailsService
│       │   └── data/           MockData
│       └── resources/
│           ├── application.properties
│           └── static/index.html   ← API Tester embutido
├── Dockerfile
├── docker-compose.yml
├── deploy.sh                        ← Script Azure CLI
└── pom.xml
```

---

## 🚀 Rotas da API

### 🔐 Auth

| Método | Rota | Descrição | Auth |
|---|---|---|---|
| `POST` | `/auth/register` | Cadastrar tutor com senha — retorna JWT | ❌ |
| `POST` | `/auth/login` | Login — retorna JWT | ❌ |

### 👤 Tutores

| Método | Rota | Descrição | Auth |
|---|---|---|---|
| `GET` | `/tutores` | Listar todos os tutores | ✅ |
| `GET` | `/tutores/{id}` | Buscar tutor por ID | ✅ |
| `POST` | `/tutores` | Cadastrar tutor (sem senha) | ✅ |
| `PUT` | `/tutores/{id}` | Atualizar tutor | ✅ |
| `DELETE` | `/tutores/{id}` | Deletar tutor | ✅ |

### 🐾 Pets

| Método | Rota | Descrição | Auth |
|---|---|---|---|
| `POST` | `/pets/tutor/{tutorId}` | Cadastrar pet — eventos iniciais sugeridos automaticamente | ✅ |
| `GET` | `/pets/{id}` | Buscar pet por ID (com `idadeAnos` calculada) | ✅ |
| `GET` | `/pets/tutor/{tutorId}?page=&size=&sort=` | Listar pets do tutor (paginado) | ✅ |
| `GET` | `/pets?especie=` | Filtrar pets por espécie | ✅ |
| `PUT` | `/pets/{id}` | Atualizar pet | ✅ |
| `DELETE` | `/pets/{id}` | Deletar pet | ✅ |

### 🏥 Eventos de Saúde

| Método | Rota | Descrição | Auth |
|---|---|---|---|
| `POST` | `/pets/{petId}/eventos` | Registrar evento | ✅ |
| `GET` | `/pets/{petId}/eventos?page=&size=&sort=` | Listar eventos (paginado) | ✅ |
| `GET` | `/pets/{petId}/eventos/pendentes` | Listar pendentes (auto-atualiza ATRASADO) | ✅ |
| `GET` | `/pets/{petId}/eventos/atrasados` | Listar atrasados | ✅ |
| `GET` | `/pets/{petId}/eventos/{id}` | Buscar evento por ID | ✅ |
| `PATCH` | `/pets/{petId}/eventos/{id}/realizado` | Marcar como realizado + agendar próxima data | ✅ |
| `PUT` | `/pets/{petId}/eventos/{id}` | Atualizar evento | ✅ |
| `DELETE` | `/pets/{petId}/eventos/{id}` | Deletar evento | ✅ |

### ⚙️ Utilitários

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/actuator/health` | Health check (`{"status":"UP"}`) |
| `GET` | `/swagger-ui.html` | Documentação interativa Swagger UI |
| `GET` | `/h2-console` | Console H2 (apenas perfil de teste) |

**Tipos de Evento disponíveis:** `VACINA` · `VERMIFUGO` · `BANHO` · `TOSA` · `CHECKUP` · `CONSULTA` · `CIRURGIA` · `MEDICAMENTO`

**Status de Evento:** `PENDENTE` → `ATRASADO` (automático) → `REALIZADO`

---

## ⚙️ Como Rodar (How to)

### Pré-requisitos

- Java 17+
- Maven 3.8+
- Docker e Docker Compose (para rodar com Oracle)

---

### Opção 1 — Docker Compose (recomendado, com Oracle XE)

Sobe o banco Oracle XE 21c e a aplicação em containers:

```bash
# Clone o repositório
git clone https://github.com/<seu-usuario>/JornadaPet-java-Sprint1.git
cd JornadaPet-java-Sprint1

# Sobe tudo (Oracle + App)
docker compose up --build
```

Aguarde o Oracle inicializar (~60s). A aplicação ficará disponível em:

| Recurso | URL |
|---|---|
| API | http://localhost:8080 |
| API Tester (frontend) | http://localhost:8080/index.html |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health Check | http://localhost:8080/actuator/health |

---

### Opção 2 — Maven (sem Docker, Oracle local)

Certifique-se de ter um Oracle XE rodando localmente ou ajuste as variáveis de ambiente:

```bash
export SPRING_DATASOURCE_URL=jdbc:oracle:thin:@localhost:1521/XEPDB1
export SPRING_DATASOURCE_USERNAME=APP_USER
export SPRING_DATASOURCE_PASSWORD=AppPassword123

mvn spring-boot:run
```

---

### Variáveis de Ambiente

| Variável | Padrão | Descrição |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:oracle:thin:@localhost:1521/XEPDB1` | URL do banco |
| `SPRING_DATASOURCE_USERNAME` | `APP_USER` | Usuário do banco |
| `SPRING_DATASOURCE_PASSWORD` | `AppPassword123` | Senha do banco |

---

### Autenticação

**1. Registre um tutor:**
```bash
POST /auth/register
{
  "nome": "Carlos Teste",
  "email": "carlos@email.com",
  "senha": "senha123",
  "telefone": "11999998888",
  "cpf": "12345678901"
}
```

**2. Faça login:**
```bash
POST /auth/login
{
  "email": "carlos@email.com",
  "senha": "senha123"
}
# Retorna: { "token": "eyJ..." }
```

**3. Use o token:**
```
Authorization: Bearer <token>
```

> Os tutores do MockData (Maria Silva, João Souza) não possuem senha. Para autenticar, use `POST /auth/register`.

---

## 🐳 Dockerfile

```dockerfile
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml pom.xml
COPY src src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
RUN chown -R appuser:appgroup /app
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

Build em dois estágios: compila com Maven e gera imagem mínima JRE. Roda com usuário não-root por segurança.

---

## 🐋 Docker Compose

```yaml
services:
  oracle-db:
    image: gvenzl/oracle-xe:21-slim
    container_name: oracle-db
    environment:
      ORACLE_PASSWORD: "OracleRoot123"
      APP_USER: "APP_USER"
      APP_USER_PASSWORD: "AppPassword123"
    volumes:
      - oracle_data:/opt/oracle/oradata
      - ./sql:/container-entrypoint-initdb.d
    healthcheck:
      test: ["CMD-SHELL", "healthcheck.sh"]
      interval: 10s
      timeout: 5s
      retries: 15
      start_period: 40s
    networks:
      - challenge_net

  jornadapet:
    build: .
    container_name: jornadapet-app
    depends_on:
      oracle-db:
        condition: service_healthy
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: "jdbc:oracle:thin:@oracle-db:1521/XEPDB1"
      SPRING_DATASOURCE_USERNAME: "APP_USER"
      SPRING_DATASOURCE_PASSWORD: "AppPassword123"
    networks:
      - challenge_net

networks:
  challenge_net:
    driver: bridge

volumes:
  oracle_data:
```

---

## ☁️ Deploy na Azure (Script CLI)

O script `deploy.sh` provisionou a infraestrutura Azure usada neste projeto:

```bash
#!/usr/bin/env bash
# Requisitos: Azure CLI instalado e autenticado (az login)

RG="rg-challenge-clyvo-vet"
LOCATION="chilecentral"
VM="vm-wise-clyvo-dev-01"

# 1. Criar Resource Group
az group create --name "$RG" --location "$LOCATION"

# 2. Criar VNet + Subnet
az network vnet create \
  --resource-group "$RG" --name "vnet_wise_dev" \
  --address-prefixes 10.10.0.0/16 \
  --subnet-name "sub_net_dev" --subnet-prefixes 10.10.1.0/24

# 3. Criar NSG
az network nsg create --resource-group "$RG" --name "nsg_portalweb_dev"

# 4. Criar VM Ubuntu 22.04 (Standard_B4ls_v2)
az vm create \
  --resource-group "$RG" --name "$VM" \
  --image Ubuntu2204 --size Standard_B4ls_v2 \
  --admin-username azureuser --generate-ssh-keys \
  --vnet-name "vnet_wise_dev" --subnet "sub_net_dev" --nsg "nsg_portalweb_dev"

# 5. Abrir portas (SSH, App, Oracle)
az vm open-port --resource-group "$RG" --name "$VM" --port 22   --priority 1000
az vm open-port --resource-group "$RG" --name "$VM" --port 8080 --priority 1001
az vm open-port --resource-group "$RG" --name "$VM" --port 1521 --priority 1002

# 6. Instalar Docker na VM
az vm run-command invoke \
  --resource-group "$RG" --name "$VM" \
  --command-id RunShellScript \
  --scripts "sudo apt-get update && sudo apt-get install -y git curl ca-certificates && curl -fsSL https://get.docker.com | sudo sh && sudo usermod -aG docker azureuser"

# 7. Exibir IP público da VM
az vm show --resource-group "$RG" --name "$VM" \
  --show-details --query publicIps --output tsv
```

**Para executar o deploy completo:**
```bash
chmod +x deploy.sh
az login
./deploy.sh
```

Após provisionamento, acesse a VM via SSH e suba os containers:
```bash
ssh azureuser@<IP_RETORNADO>
git clone https://github.com/<seu-usuario>/JornadaPet-java-Sprint1.git
cd JornadaPet-java-Sprint1
docker compose up --build -d
```

---

## 📁 Documentação

| Arquivo | Descrição |
|---|---|
| [`/documentos/JornadaPet_Postman_Collection.json`](./documentos/JornadaPet_Postman_Collection.json) | Coleção completa — importe no Postman |
| [`/documentos/diagrama-classes.png`](./documentos/diagrama-classes.png) | Diagrama de Classes (entidades, repos, services, controllers) |
| [`/documentos/der.png`](./documentos/der.png) | DER — Diagrama Entidade-Relacionamento |
| [`/documentos/cronograma-sprint1.md`](./documentos/cronograma-sprint1.md) | Cronograma detalhado com divisão de tarefas |

---

## 🛠️ Tecnologias

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 17 | Linguagem |
| Spring Boot | 3.3.5 | Framework principal |
| Spring Security | — | Autenticação JWT |
| Spring Data JPA | — | Persistência |
| Oracle XE | 21c | Banco de dados (produção) |
| jjwt | 0.11.5 | Geração e validação de tokens |
| Springdoc OpenAPI | 2.6.0 | Swagger UI |
| Lombok | — | Redução de boilerplate |
| Docker / Compose | — | Containerização |
| Azure CLI | — | Provisionamento de infraestrutura |

---

*JornadaPet — FIAP 2026 | Challenge Clyvo Vet | 2º Ano ADS — Turmas de Fevereiro*
