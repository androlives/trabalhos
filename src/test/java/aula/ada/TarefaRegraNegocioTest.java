package aula.ada;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class TarefaRegraNegocioTest {

    @Test
    void tarefaCanceladaNaoPodeSerConcluida() {
        int id = criarTarefa("{\"titulo\":\"Tarefa cancelada\",\"status\":\"CANCELADA\",\"prioridade\":\"BAIXA\"}");

        given().contentType(ContentType.JSON)
                .body("{\"titulo\":\"Tarefa cancelada\",\"status\":\"CONCLUIDA\"}")
                .when().put("/tarefas/" + id)
                .then().statusCode(409)
                .body("codigo", is("TRANSICAO_TAREFA_INVALIDA"))
                .body("mensagem", notNullValue())
                .body("timestamp", notNullValue());
    }

    @Test
    void tarefaEmAndamentoPodeSerConcluida() {
        int id = criarTarefa("{\"titulo\":\"Tarefa em andamento\",\"status\":\"EM_ANDAMENTO\",\"prioridade\":\"BAIXA\"}");

        given().contentType(ContentType.JSON)
                .body("{\"titulo\":\"Tarefa em andamento\",\"status\":\"CONCLUIDA\"}")
                .when().put("/tarefas/" + id)
                .then().statusCode(200)
                .body("status", is("CONCLUIDA"));
    }

    private int criarTarefa(String json) {
        return given().contentType(ContentType.JSON).body(json)
                .when().post("/tarefas")
                .then().statusCode(201)
                .extract().path("id");
    }
}
