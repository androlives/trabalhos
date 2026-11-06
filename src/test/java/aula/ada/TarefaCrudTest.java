package aula.ada;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class TarefaCrudTest {

    @Test
    void criarAlterarConsultarEExcluirTarefa() {
        int id = given().contentType(ContentType.JSON)
                .body("{\"titulo\":\"Tarefa original\",\"prioridade\":\"BAIXA\"}")
                .when().post("/tarefas")
                .then().statusCode(201)
                .header("Location", notNullValue())
                .body("status", is("ABERTA"))
                .extract().path("id");

        given().contentType(ContentType.JSON).body("{\"titulo\":\"Tarefa alterada\"}")
                .when().put("/tarefas/" + id)
                .then().statusCode(200);

        given().when().get("/tarefas/" + id)
                .then().statusCode(200)
                .body("titulo", is("Tarefa alterada"));

        given().when().delete("/tarefas/" + id).then().statusCode(204);
        given().when().get("/tarefas/" + id).then().statusCode(404);
    }

    @Test
    void listarRetornaTarefasCriadas() {
        given().contentType(ContentType.JSON)
                .body("{\"titulo\":\"Tarefa listada\",\"prioridade\":\"MEDIA\"}")
                .when().post("/tarefas").then().statusCode(201);

        given().when().get("/tarefas")
                .then().statusCode(200)
                .body("titulo", hasItem("Tarefa listada"));
    }
}
