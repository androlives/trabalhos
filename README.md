# Controle de Tarefas

API REST para controle de tarefas, desenvolvida como projeto final da disciplina Introdução ao Quarkus.

**Equipe:** Nome da equipe
**Integrantes:** André Rodrigues de Oliveira, Everton Lucas Fernandes, Danilo Tanaka Vieira Marques, Rhuan Elias Botelho de Azeredo, Thiago Banfi Credico

## Problema resolvido

A API permite cadastrar, consultar, alterar e excluir tarefas, cada uma com título, descrição, status e prioridade. Uma regra de negócio impede que uma tarefa **cancelada** seja marcada como **concluída**. Quando a tarefa é criada sem prioridade, a API consulta um serviço externo de sugestão de prioridade; se esse serviço estiver fora do ar, a tarefa é criada com prioridade `MEDIA`.

## Tecnologias

- Java 21 e Maven
- Quarkus 3.40.1.1 (Quarkus REST, Hibernate ORM com Panache, REST Client, Fault Tolerance, Health, Micrometer, Hibernate Validator)
- Banco H2 em memória
- Lombok

## Como executar

> O mock **não sobe automaticamente** com a API. Ele é um processo separado e precisa ser iniciado manualmente.

Serviço externo (mock de prioridade, porta 8089), em um terminal:

```bash
java mock/MockController.java 8090
```

No Windows (`cmd.exe`), na raiz do projeto:

```bat
cd /d ".\trabalhos"
java mock/MockController.java 8090
```

API (profile `dev`, porta 8080), em outro terminal:

```bash
./mvnw quarkus:dev
```

Para simular o serviço externo fora do ar, basta encerrar o mock com Ctrl+C.

Ou via Postman, a API externa/mock após iniciar MockController.java 8090 é capaz de simular o Ctrl+C em e "PrioridadeMock,java"    

Testes automatizados:

```bash
./mvnw clean test
```

No profile `test` a URL do serviço externo aponta para uma porta sem nenhum serviço ouvindo, o que simula a indisponibilidade e exercita o fallback.

## Banco de dados

H2 em memória, criado a cada inicialização (`drop-and-create`). Não há instalação externa.

## Configuração e profiles

A URL do serviço de prioridade fica no `application.properties`, nunca em classe Java:

| Profile | URL |
|---|---|
| `dev` | `http://localhost:8089` |
| `test` | `http://localhost:9` |
| `prod` | `http://prioridade-api:8089` |

Uma variável de ambiente sobrescreve o valor do arquivo. Exemplo, com o mock rodando em outra porta:

```bash
java mock/PrioridadeMock.java 9090
QUARKUS_REST_CLIENT_PRIORIDADE_API_URL=http://localhost:9090 ./mvnw quarkus:dev
```

Para executar no profile `prod`:

```bash
./mvnw package
QUARKUS_REST_CLIENT_PRIORIDADE_API_URL=http://localhost:8089 java -jar target/quarkus-app/quarkus-run.jar
```

## Endpoints

| Método | Rota | Respostas |
|---|---|---|
| GET | `/tarefas` | 200 |
| GET | `/tarefas/{id}` | 200, 404 |
| POST | `/tarefas` | 201 (com `Location`), 400 |
| PUT | `/tarefas/{id}` | 200, 400, 404, 409 |
| DELETE | `/tarefas/{id}` | 204, 404 |
| GET | `/q/health/live` | 200 |
| GET | `/q/health/ready` | 200, ou 503 quando desativado |
| GET | `/q/metrics` | 200 |
| PUT | `/admin/readiness/{up\|down}` | liga ou desliga o readiness (apenas para demonstração) |

Exemplo de criação:

```bash
curl -i -X POST localhost:8080/tarefas \
  -H 'Content-Type: application/json' \
  -d '{"titulo":"Estudar Quarkus","descricao":"Revisar as aulas","prioridade":"ALTA"}'
```

Se `prioridade` não for informada, ela é obtida do serviço externo (ou `MEDIA`, no fallback). Se `status` não for informado, a tarefa nasce `ABERTA`.

### Formato dos erros

Todos os erros seguem o mesmo formato:

```json
{
  "codigo": "TAREFA_NAO_ENCONTRADA",
  "mensagem": "Tarefa não encontrada com ID: 999999",
  "timestamp": "2026-11-06T14:30:00Z"
}
```

Nos erros de validação (400), o campo `detalhes` lista cada violação.

| Situação | Status | Código |
|---|---|---|
| Dados inválidos | 400 | `VALIDACAO_INVALIDA` |
| Corpo malformado ou valor de enum inválido | 400 | `CORPO_INVALIDO` |
| Tarefa inexistente | 404 | `TAREFA_NAO_ENCONTRADA` |
| Cancelada para concluída | 409 | `TRANSICAO_TAREFA_INVALIDA` |

## Organização do código

Pacote base `aula.ada`:

| Pacote | Conteúdo |
|---|---|
| `rest` | `TarefaResource` e `ReadinessAdminResource` |
| `service` | `TarefaService`, regras de negócio e exceções |
| `repository` | `TarefaRepository` (Panache) |
| `entity` | `TarefaEntity` |
| `dto` | `TarefaRequest`, `TarefaResponse`, `ErroResponse` |
| `domain` | `StatusTarefa`, `PrioridadeTarefa` |
| `client` | REST Client do serviço de prioridade e a classe com retry e fallback |
| `platform.exception` | `ExceptionMapper` de cada erro |
| `platform.health` | liveness e readiness |
| `platform.metrics` | métrica de negócio |

O fluxo é `Resource` → `Service` → `Repository`. O Resource só trata HTTP, a regra de negócio está no Service e o acesso a dados no Repository.

## Testes automatizados

| Classe | O que verifica |
|---|---|
| `TarefaCrudTest` | CRUD completo, `Location`, 204, 404 final e PUT confirmado por nova consulta |
| `TarefaRegraNegocioTest` | Cancelada para concluída retorna 409; em andamento para concluída retorna 200 |
| `TratamentoErrosTest` | 400 de validação, 400 de status inválido e 404, todos no formato padrão |
| `PrioridadeIntegracaoTest` | Serviço externo fora do ar usa o fallback; prioridade informada é mantida |
| `ObservabilidadeTest` | Readiness DOWN com liveness UP; contador de tarefas criadas aumenta |

## Demonstração com o Postman

A coleção está em `postman/projeto-final.postman_collection.json`. Importe no Postman, com a API e o mock no ar. A variável `baseUrl` já aponta para `http://localhost:8080`.

| Pasta | Conteúdo |
|---|---|
| 1. CRUD e transações | POST, GET, PUT, GET de confirmação, DELETE e GET 404 |
| 2. Regra de negócio | Tarefa cancelada não pode ser concluída (409) |
| 3. Validação e erros | 400 e 404 no formato padrão |
| 4. Integração externa | Mock ligado (prioridade `ALTA`) e mock desligado (fallback `MEDIA`) |
| 5. Health e métricas | Liveness, readiness DOWN e contador antes e depois |

As pastas 1, 2, 3 e 5 podem ser executadas inteiras pelo Runner. Na pasta 4, execute o primeiro request com o mock ligado, encerre o mock e execute o segundo.

## Evidências por requisito

Prints e saídas de cada demonstração estão em [`docs/evidencias.md`](docs/evidencias.md).

| Req. | Descrição | Onde está |
|---|---|---|
| 01 | Recursos e métodos HTTP | `TarefaResource`; Postman, pasta 1 |
| 02 | Status 200, 201, 204, 400, 404 e `Location` | `TarefaResource` e mappers; Postman, pastas 1 e 3; `TarefaCrudTest` |
| 03 | Lombok | `TarefaRequest`, `TarefaResponse` e `TarefaEntity` (ver nota abaixo) |
| 04 | Quarkus REST e JSON | `TarefaResource` com `@Path`, `@GET`, `@POST`, `@PUT`, `@DELETE`, `@Produces` e `@Consumes` |
| 05 | CDI e regra de negócio | `TarefaService`; `TarefaRegraNegocioTest`; Postman, pasta 2 |
| 06 | REST Client | `PrioridadeClient` e `PrioridadeGateway`; mock em `mock/`; Postman, pasta 4 |
| 07 | Configuração e profiles | `application.properties` (`dev`, `test`, `prod`) e variável de ambiente |
| 08 | Liveness e readiness | `platform.health`; `ObservabilidadeTest`; Postman, pasta 5 |
| 09 | Retry e fallback | `PrioridadeGateway`; `PrioridadeIntegracaoTest`; Postman, pasta 4 |
| 10 | Métrica de negócio | `TarefaMetrics` (`tarefas_criadas_total`); `ObservabilidadeTest`; Postman, pasta 5 |
| 11 | Entidade JPA | `TarefaEntity`, separada dos DTOs; tabela `tarefas` criada pelo Hibernate |
| 12 | CRUD com Panache | `TarefaRepository`; `TarefaCrudTest` |
| 13 | Transações | `@Transactional` em `criar`, `atualizar` e `excluir`; PUT seguido de GET em `TarefaCrudTest` |
| 14 | Bean Validation | `@Valid` no Resource; `@NotBlank` e `@Size` em `TarefaRequest`; `TratamentoErrosTest` |
| 15 | ExceptionMapper | `TransicaoTarefaInvalidaMapper` (409), `TarefaNaoEncontradaMapper` (404) e `ValidacaoMapper` (400) |
| 16 | Erro padronizado | `ErroResponse`; `TratamentoErrosTest`; Postman, pasta 3 |

### Nota sobre o Lombok (req. 03)

Nos DTOs e na entidade usamos `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor` e `@AllArgsConstructor`. O Lombok gera esses métodos e construtores na compilação, o que elimina código repetitivo. O `@NoArgsConstructor` é necessário porque o JPA e o Jackson instanciam as classes por reflexão. O `@Builder` é usado nas conversões entre entidade e DTO. Não usamos `@Data` na entidade JPA, porque ele gera `equals` e `hashCode` a partir de todos os campos, o que é problemático para entidades persistentes. `TarefaResponse` só tem `@Getter`, por ser somente leitura.

### Decisões de projeto

- O readiness não depende do serviço externo de prioridade: como há fallback, a indisponibilidade dele não deve retirar a API de operação.
- O liveness indica que o processo está vivo; o readiness indica que a API pode receber tráfego e é controlável por `/admin/readiness/{up|down}`.
- A chamada ao serviço externo usa retry (2 tentativas adicionais) e fallback para `MEDIA`.
