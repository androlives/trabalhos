# Controle de Tarefas — Quarkus

API de tarefas com H2 e consulta HTTP de sugestão de prioridade. A Task 4 acrescenta REST Client, profiles, retry, fallback e mock local reproduzível.

## Executar no Windows / PowerShell

Use **JDK 21** e execute os comandos na raiz do projeto. O Maven Wrapper está incluído. Se necessário, aponte para sua instalação do JDK:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
```

Terminal 1 — API na porta 8080:

```powershell
.\mvnw.cmd quarkus:dev
```

Terminal 2 — mock HTTP na porta 8081:

```powershell
java mock-prioridade/PrioridadeMock.java
```

Terminal 3 — consulta passando pela API e pelo REST Client:

```powershell
curl.exe -i --get 'http://localhost:8080/prioridades/sugestao' --data-urlencode 'titulo=Corrigir erro urgente'
```

Com o mock disponível, o esperado é HTTP **200**:

```json
{"prioridade":"ALTA","origem":"SERVICO_EXTERNO"}
```

Encerre o mock com `Ctrl+C` e repita a consulta. Após as tentativas, o esperado é HTTP **503**:

```json
{"prioridade":"MEDIA","origem":"FALLBACK"}
```

O fallback explicita a indisponibilidade da dependência, como no catálogo remoto do professor. `MEDIA` é a sugestão de contingência; o status 503 não representa uma consulta externa bem-sucedida. Reiniciar o mock permite que consultas seguintes voltem a responder 200.

## Contratos

| Operação | Endpoint | Comportamento |
| --- | --- | --- |
| Sugerir prioridade | `GET /prioridades/sugestao?titulo=...` | 200 remoto, 503 fallback; título obrigatório, não branco e até 100 caracteres |
| Listar tarefas | `GET /tarefas` | CRUD existente |
| Buscar tarefa | `GET /tarefas/{id}` | 200 ou 404 |
| Criar tarefa | `POST /tarefas` | 201 e `Location` |
| Atualizar tarefa | `PUT /tarefas/{id}` | 200; CANCELADA → CONCLUIDA continua retornando 409 |
| Excluir tarefa | `DELETE /tarefas/{id}` | 204 ou 404 |

A consulta de prioridade é **independente do CRUD**: não cria nem altera tarefas e não preenche automaticamente a prioridade de um POST/PUT. É possível consultar a sugestão e enviar a prioridade desejada ao CRUD. O banco permanece H2 em memória, conforme a configuração didática existente, e é recriado ao iniciar a aplicação.

## Mock e tolerância a falhas

O mock usa o servidor HTTP do próprio JDK, sem Docker:

```text
java mock-prioridade/PrioridadeMock.java [porta] [falhasIniciais]
```

Os argumentos opcionais têm padrões `8081` e `0`; não digite os colchetes. Para demonstrar falha transitória:

```powershell
java mock-prioridade/PrioridadeMock.java 8081 2
```

A instância recém-iniciada responde 503 nas duas primeiras consultas válidas e 200 na terceira. Faça uma única consulta pela API para observar os logs `503`, `503`, `200`. Não consulte o mock diretamente antes dessa prova: o contador é compartilhado por todas as chamadas.

Títulos com `urgente`, `critico` ou `crítico` recebem `ALTA`; com `opcional`, `BAIXA`; os demais recebem `MEDIA`.

O serviço realiza **uma tentativa inicial e até duas novas tentativas**, com intervalo de 100 ms e sem jitter. Os timeouts de conexão e leitura são de 1.000 ms cada, por tentativa; não são um limite global de um segundo para a operação. Falha HTTP, indisponibilidade, timeout ou resposta externa sem prioridade válida esgotam as tentativas e acionam o fallback. O retry atua somente na consulta externa, sem repetir escritas no banco.

## Profiles e variável de ambiente

| Profile | URL padrão do serviço externo |
| --- | --- |
| `dev`, `test`, `prod` | `http://127.0.0.1:8081` |
| `staging` | `http://127.0.0.1:8082` |

`PRIORIDADE_LOCAL_URL` sobrepõe a URL em todos os profiles. A URL externa não fica fixa nas classes Java da aplicação.

Para avaliar staging com o JAR, encerre a API anterior e remova a variável antes de empacotar:

```powershell
Remove-Item Env:PRIORIDADE_LOCAL_URL -ErrorAction SilentlyContinue
.\mvnw.cmd package
java '-Dquarkus.profile=staging' -jar target/quarkus-app/quarkus-run.jar
```

Em outro terminal, inicie `java mock-prioridade/PrioridadeMock.java 8082` e consulte a API na porta 8080. Para comprovar sobrescrita, encerre a API, inicie um mock em 8091 e reinicie:

```powershell
$env:PRIORIDADE_LOCAL_URL = 'http://127.0.0.1:8091'
java '-Dquarkus.profile=staging' -jar target/quarkus-app/quarkus-run.jar
```

A variável deve estar no terminal que inicia a API. Ao terminar, remova-a com `Remove-Item Env:PRIORIDADE_LOCAL_URL -ErrorAction SilentlyContinue`.

## Arquitetura da Task 4

A nova funcionalidade segue **Package by Feature + Boundary / Domain / Integration**, conforme o módulo remoto das aulas:

```text
aula.ada.prioridades
├── boundary                 # Resource e DTO HTTP
├── domain                   # Serviço CDI, resultado e origem
└── integration.prioridade   # REST Client e contrato externo
```

O fluxo é `PrioridadeResource → PrioridadeService → PrioridadeGateway → mock HTTP`. O serviço retorna `PrioridadeResultado`, sem depender de DTO HTTP ou `Response`; o boundary converte o resultado e decide o status. O enum legado `PrioridadeTarefa` é compartilhado para manter compatibilidade com o CRUD.

A organização acima cobre a **nova feature**. O CRUD ainda usa os pacotes globais `rest`, `service`, `repository`, `entity` e `dto`; sua migração para a organização por funcionalidade precisa ser alinhada separadamente com a equipe.

## Testes e avaliação

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

Os testes da integração controlam um servidor HTTP local, sem exigir o mock manual. O roteiro dos requisitos **06, 07 e 09** está em [docs/evidencias.md](docs/evidencias.md).

Importe [postman/task-04.postman_collection.json](postman/task-04.postman_collection.json). A variável `base_url` tem padrão `http://localhost:8080`. Execute cada pasta separadamente, preparando o mock conforme a descrição; a collection não inicia ou encerra processos. Ela cobre a Task 4 e não substitui a collection consolidada da Task 5.

Referências: [Quarkus REST Client](https://quarkus.io/guides/rest-client/) e [SmallRye Fault Tolerance](https://quarkus.io/guides/smallrye-fault-tolerance/).
