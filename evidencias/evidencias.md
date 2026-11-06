# Evidências dos requisitos 01 a 16

Projeto: Controle de Tarefas (Introdução ao Quarkus)
Equipe: Nome da equipe

Como reproduzir: subir o mock (`java mock/PrioridadeMock.java`) e a API (`./mvnw quarkus:dev`). As requisições estão na coleção `postman/projeto-final.postman_collection.json`. As imagens ficam em `docs/img/`.

## Resumo

| Req. | Descrição | Responsável | Evidência |
|---|---|---|---|
| 01 | Recursos e métodos HTTP | | [ver](#requisito-01) |
| 02 | Códigos de status e `Location` | | [ver](#requisito-02) |
| 03 | Lombok | | [ver](#requisito-03) |
| 04 | Quarkus REST e JSON | | [ver](#requisito-04) |
| 05 | CDI e regra de negócio | | [ver](#requisito-05) |
| 06 | REST Client | | [ver](#requisito-06) |
| 07 | Configuração e profiles | | [ver](#requisito-07) |
| 08 | Liveness e readiness | | [ver](#requisito-08) |
| 09 | Retry e fallback | | [ver](#requisito-09) |
| 10 | Métrica de negócio | | [ver](#requisito-10) |
| 11 | Entidade JPA | | [ver](#requisito-11) |
| 12 | CRUD com Panache | | [ver](#requisito-12) |
| 13 | Transações e atualização | | [ver](#requisito-13) |
| 14 | Bean Validation | | [ver](#requisito-14) |
| 15 | ExceptionMapper | | [ver](#requisito-15) |
| 16 | Erro padronizado | | [ver](#requisito-16) |

---

## Aulas 01 e 02: HTTP, REST e Lombok

### Requisito 01
**Recursos e métodos HTTP**

- Código: `aula.ada.rest.TarefaResource`
- Rotas: `GET /tarefas`, `GET /tarefas/{id}`, `POST /tarefas`, `PUT /tarefas/{id}`, `DELETE /tarefas/{id}`
- Postman: pasta "1. CRUD e transações"

Evidência:

![Requisições do CRUD no Postman](img/req-01.png)

### Requisito 02
**Códigos de status e `Location`**

- Código: `TarefaResource` e mappers em `aula.ada.platform.exception`
- Status demonstrados: 200, 201 (com `Location`), 204, 400 e 404
- Postman: pastas 1 e 3

Evidência:

| Status | Requisição | Print |
|---|---|---|
| 200 | `GET /tarefas` | ![200](img/req-02-200.png) |
| 201 + `Location` | `POST /tarefas` | ![201](img/req-02-201.png) |
| 204 | `DELETE /tarefas/{id}` | ![204](img/req-02-204.png) |
| 400 | `POST /tarefas` com título vazio | ![400](img/req-02-400.png) |
| 404 | `GET /tarefas/999999` | ![404](img/req-02-404.png) |

### Requisito 03
**Lombok aplicado conscientemente**

- Código: `TarefaRequest`, `TarefaResponse`, `TarefaEntity`
- Anotações usadas: `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- Não foi usado `@Data` na entidade JPA.

Explicação do código gerado:

> Escrever aqui o que cada anotação gera e por que foi escolhida (veja a nota sobre Lombok no README).

Evidência:

![Classe anotada](img/req-03.png)

---

## Aula 03: Quarkus REST, CDI e REST Client

### Requisito 04
**API Quarkus/JAX-RS**

- Código: `TarefaResource` (`@Path`, `@GET`, `@POST`, `@PUT`, `@DELETE`, `@Produces`, `@Consumes`)

Evidência (resposta JSON):

![Resposta JSON](img/req-04.png)

### Requisito 05
**CDI e regra de negócio**

- Código: `TarefaResource` injeta `TarefaService` (`@ApplicationScoped`)
- Regra: tarefa `CANCELADA` não pode passar para `CONCLUIDA`
- Teste: `TarefaRegraNegocioTest`
- Postman: pasta "2. Regra de negócio"

Evidência:

![Regra de negócio retornando 409](img/req-05.png)

### Requisito 06
**Consumo de API externa**

- Código: `aula.ada.client.PrioridadeClient` e `PrioridadeGateway`
- Mock: `mock/PrioridadeMock.java` (porta 8089)
- Cenário: `POST /tarefas` sem `prioridade` e com título contendo "urgente" retorna `ALTA`
- Postman: pasta "4. Integração externa", primeira requisição

Comando para reproduzir:

```bash
curl -i -X POST localhost:8080/tarefas -H 'Content-Type: application/json' \
  -d '{"titulo":"Corrigir bug urgente"}'
```

Evidência (resposta da API e log do mock):

![Resposta com prioridade vinda do mock](img/req-06.png)

---

## Aula 04: MicroProfile, observabilidade e resiliência

### Requisito 07
**Configurações e profiles**

- Arquivo: `src/main/resources/application.properties` (URL do serviço por profile: `dev`, `test`, `prod`)
- Variável de ambiente sobrescrevendo o valor: `QUARKUS_REST_CLIENT_PRIORIDADE_API_URL`

Cenário 1: profile `dev` (mock na porta 8089).

![Execução em dev](img/req-07-dev.png)

Cenário 2: profile `prod` com variável de ambiente.

```bash
./mvnw package
QUARKUS_REST_CLIENT_PRIORIDADE_API_URL=http://localhost:8089 java -jar target/quarkus-app/quarkus-run.jar
```

![Execução em prod](img/req-07-prod.png)

Cenário 3: variável de ambiente sobrescrevendo o `dev` (mock na porta 9090).

```bash
java mock/PrioridadeMock.java 9090
QUARKUS_REST_CLIENT_PRIORIDADE_API_URL=http://localhost:9090 ./mvnw quarkus:dev
```

![Sobrescrita por variável de ambiente](img/req-07-env.png)

### Requisito 08
**Liveness e readiness**

- Código: `aula.ada.platform.health` (`AplicacaoLivenessCheck`, `ProntidaoReadinessCheck`, `ProntidaoControle`)
- Teste: `ObservabilidadeTest`
- Postman: pasta "5. Health e métricas"

Quando cada um fica DOWN:

> Liveness: indica que o processo está vivo; se ficasse DOWN, a plataforma reiniciaria a aplicação.
> Readiness: indica que a API pode receber tráfego; se ficar DOWN, ela sai do balanceamento sem ser reiniciada. Aqui é controlada por `PUT /admin/readiness/{up|down}`.

Evidência:

| Situação | Requisição | Print |
|---|---|---|
| Liveness UP | `GET /q/health/live` | ![live](img/req-08-live.png) |
| Readiness UP | `GET /q/health/ready` | ![ready up](img/req-08-ready-up.png) |
| Readiness DOWN (503) | `PUT /admin/readiness/down` e `GET /q/health/ready` | ![ready down](img/req-08-ready-down.png) |
| Liveness continua UP | `GET /q/health/live` | ![live com ready down](img/req-08-live-up.png) |

### Requisito 09
**Tolerância a falhas**

- Código: `aula.ada.client.PrioridadeGateway` (`@Retry` e `@Fallback`)
- Teste: `PrioridadeIntegracaoTest`
- Postman: pasta "4. Integração externa"

Cenário externo UP (mock ligado): prioridade `ALTA`.

![Serviço UP](img/req-09-up.png)

Cenário externo DOWN (mock encerrado): `201` com prioridade `MEDIA`.

![Serviço DOWN com fallback](img/req-09-down.png)

Log da aplicação mostrando as tentativas e o fallback:

![Log de retry e fallback](img/req-09-log.png)

### Requisito 10
**Métrica de negócio**

- Código: `aula.ada.platform.metrics.TarefaMetrics` (contador `tarefas_criadas_total`)
- Teste: `ObservabilidadeTest`
- Postman: pasta "5. Health e métricas"

Comando:

```bash
curl -s localhost:8080/q/metrics | grep tarefas_criadas_total
```

| Momento | Valor | Print |
|---|---|---|
| Antes de criar uma tarefa | | ![antes](img/req-10-antes.png) |
| Depois de criar uma tarefa | | ![depois](img/req-10-depois.png) |

---

## Aula 05: Hibernate ORM e Panache

### Requisito 11
**Entidade JPA**

- Código: `aula.ada.entity.TarefaEntity` (`@Entity`, `@Id`, campos do domínio)
- DTOs separados: `TarefaRequest` e `TarefaResponse`

Evidência (classe e log do Hibernate com o `create table tarefas`):

![Entity](img/req-11-entity.png)

![Tabela criada](img/req-11-tabela.png)

### Requisito 12
**CRUD com Panache Repository**

- Código: `aula.ada.repository.TarefaRepository` (`PanacheRepository<TarefaEntity>`)
- Teste: `TarefaCrudTest`

Evidência (CRUD real, dados gravados e lidos do banco):

![CRUD com Panache](img/req-12.png)

### Requisito 13
**Transações e atualização**

- Código: `TarefaService` com `@Transactional` em `criar`, `atualizar` e `excluir`
- Teste: `TarefaCrudTest`
- Sequência: `POST`, `PUT`, `GET` (confirma a alteração), `DELETE`, `GET` (404)

Evidência:

| Passo | Print |
|---|---|
| POST cria | ![post](img/req-13-post.png) |
| PUT altera | ![put](img/req-13-put.png) |
| GET confirma | ![get](img/req-13-get.png) |
| DELETE remove | ![delete](img/req-13-delete.png) |
| GET final retorna 404 | ![get 404](img/req-13-404.png) |

---

## Aula 06: Validação e tratamento de erros

### Requisito 14
**Bean Validation**

- Código: `@Valid` em `TarefaResource`; `@NotBlank` e `@Size` em `TarefaRequest`
- Teste: `TratamentoErrosTest`

Evidência (requisição inválida rejeitada com 400):

![Validação retornando 400](img/req-14.png)

### Requisito 15
**ExceptionMapper personalizado**

- Exceções de negócio: `TarefaNaoEncontradaException` e `TransicaoTarefaInvalidaException`
- Mappers em `aula.ada.platform.exception`: `TarefaNaoEncontradaMapper` (404), `TransicaoTarefaInvalidaMapper` (409) e `ValidacaoMapper` (400)

Evidência:

| Cenário | Status | Print |
|---|---|---|
| Tarefa inexistente | 404 | ![404](img/req-15-404.png) |
| Cancelada para concluída | 409 | ![409](img/req-15-409.png) |

### Requisito 16
**Resposta de erro padronizada**

- Código: `aula.ada.dto.ErroResponse` (`codigo`, `mensagem`, `timestamp` e `detalhes` quando necessário)
- Teste: `TratamentoErrosTest`
- Postman: pasta "3. Validação e erros"

Evidência (dois erros distintos com o mesmo formato):

![Erro 400](img/req-16-400.png)

![Erro 404](img/req-16-404.png)

---

## Testes automatizados

Comando: `./mvnw clean test`

![Resultado dos testes](img/testes.png)
