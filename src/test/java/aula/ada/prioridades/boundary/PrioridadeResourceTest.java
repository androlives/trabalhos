package aula.ada.prioridades.boundary;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.path.json.JsonPath;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(value = PrioridadeTestResource.class, restrictToAnnotatedClass = true)
class PrioridadeResourceTest {
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

    @ConfigProperty(name = "quarkus.rest-client.prioridade-local.url")
    String mockUrl;

    @BeforeEach
    void prepararServicoExterno() throws IOException, InterruptedException {
        controlar("SUCESSO");
    }

    @Test
    void consultaPrioridadePorHttpPreservandoTitulo() throws IOException, InterruptedException {
        String titulo = "Revisão urgente: ação & integração";
        given().queryParam("titulo", titulo)
                .when().get("/prioridades/sugestao")
                .then().statusCode(200).contentType("application/json")
                .body("prioridade", is("ALTA"))
                .body("origem", is("SERVICO_EXTERNO"));

        JsonPath estado = estado();
        assertEquals(1, estado.getInt("chamadas"));
        assertEquals(titulo, estado.getString("titulo"));
    }

    @Test
    void repeteDuasFalhasTransitoriasSemCriarTarefas() throws IOException, InterruptedException {
        controlar("DUAS_FALHAS");
        int quantidadeAntes = quantidadeTarefas();
        given().queryParam("titulo", "Tarefa urgente")
                .when().get("/prioridades/sugestao")
                .then().statusCode(200)
                .body("prioridade", is("ALTA"))
                .body("origem", is("SERVICO_EXTERNO"));

        assertEquals(3, estado().getInt("chamadas"));
        assertEquals(quantidadeAntes, quantidadeTarefas());
    }

    @Test
    void sinalizaFallbackNaFalhaPermanenteERecuperaNaProximaConsulta() throws IOException, InterruptedException {
        controlar("INDISPONIVEL");
        consultarFallback();
        assertEquals(3, estado().getInt("chamadas"));

        controlar("SUCESSO");
        given().queryParam("titulo", "Tarefa recuperada")
                .when().get("/prioridades/sugestao")
                .then().statusCode(200)
                .body("prioridade", is("ALTA"))
                .body("origem", is("SERVICO_EXTERNO"));
        assertEquals(1, estado().getInt("chamadas"));
    }

    @Test
    void usaFallbackQuandoContratoExternoTrazPrioridadeInvalida() throws IOException, InterruptedException {
        controlar("INVALIDO");
        consultarFallback();
        assertEquals(3, estado().getInt("chamadas"));
    }

    @Test
    void usaFallbackQuandoServicoExcedeTimeout() throws IOException, InterruptedException {
        controlar("LENTO");
        consultarFallback();
        assertEquals(3, estado().getInt("chamadas"));
    }

    @Test
    void rejeitaTituloAusenteBrancoOuLongoSemChamarServicoExterno() throws IOException, InterruptedException {
        given().when().get("/prioridades/sugestao").then().statusCode(400);
        for (String titulo : new String[]{"   ", "x".repeat(101)}) {
            given().queryParam("titulo", titulo)
                    .when().get("/prioridades/sugestao")
                    .then().statusCode(400);
        }
        assertEquals(0, estado().getInt("chamadas"));
    }

    @Test
    void crudContinuaIndependenteDoServicoExternoIndisponivel() throws IOException, InterruptedException {
        controlar("INDISPONIVEL");
        int quantidadeAntes = quantidadeTarefas();
        var criada = given().contentType("application/json")
                .body("{\"titulo\":\"Tarefa independente\",\"prioridade\":\"BAIXA\"}")
                .when().post("/tarefas")
                .then().statusCode(201);
        long id = criada.extract().jsonPath().getLong("id");
        try {
            criada.header("Location", endsWith("/tarefas/" + id));
            given().when().get("/tarefas/{id}", id)
                    .then().statusCode(200).body("prioridade", is("BAIXA"));
            given().contentType("application/json")
                    .body("{\"titulo\":\"Tarefa atualizada\",\"status\":\"CONCLUIDA\",\"prioridade\":\"ALTA\"}")
                    .when().put("/tarefas/{id}", id)
                    .then().statusCode(200);
            given().when().get("/tarefas/{id}", id)
                    .then().statusCode(200)
                    .body("titulo", is("Tarefa atualizada"))
                    .body("status", is("CONCLUIDA"))
                    .body("prioridade", is("ALTA"));
            assertEquals(quantidadeAntes + 1, quantidadeTarefas());
        } finally {
            given().when().delete("/tarefas/{id}", id).then().statusCode(204);
        }
        given().when().get("/tarefas/{id}", id).then().statusCode(404);
        assertEquals(quantidadeAntes, quantidadeTarefas());
        assertEquals(0, estado().getInt("chamadas"));
    }

    private static void consultarFallback() {
        given().queryParam("titulo", "Tarefa urgente")
                .when().get("/prioridades/sugestao")
                .then().statusCode(503).contentType("application/json")
                .body("prioridade", is("MEDIA"))
                .body("origem", is("FALLBACK"));
    }

    private static int quantidadeTarefas() {
        return given().when().get("/tarefas")
                .then().statusCode(200).extract().jsonPath().getList("$").size();
    }

    private void controlar(String cenario) throws IOException, InterruptedException {
        consultarControle("?cenario=" + cenario);
    }

    private JsonPath estado() throws IOException, InterruptedException {
        return JsonPath.from(consultarControle(""));
    }

    private String consultarControle(String query) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(mockUrl + "/controle" + query))
                .timeout(Duration.ofSeconds(3)).GET().build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        return response.body();
    }
}
