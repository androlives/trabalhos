package aula.ada;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class TratamentoErrosTest {

    @Test
    void tituloVazioRetorna400ComDuasViolacoes() {
        given().contentType(ContentType.JSON).body("{\"titulo\":\"\"}")
                .when().post("/tarefas")
                .then().statusCode(400)
                .body("codigo", is("VALIDACAO_INVALIDA"))
                .body("mensagem", notNullValue())
                .body("timestamp", notNullValue())
                .body("detalhes", hasSize(2));
    }

    @Test
    void statusInvalidoRetorna400NoMesmoFormato() {
        given().contentType(ContentType.JSON).body("{\"titulo\":\"Teste\",\"status\":\"XPTO\"}")
                .when().post("/tarefas")
                .then().statusCode(400)
                .body("codigo", notNullValue())
                .body("mensagem", notNullValue())
                .body("timestamp", notNullValue());
    }

    @Test
    void tarefaInexistenteRetorna404NoMesmoFormato() {
        given().when().get("/tarefas/999999")
                .then().statusCode(404)
                .body("codigo", is("TAREFA_NAO_ENCONTRADA"))
                .body("mensagem", notNullValue())
                .body("timestamp", notNullValue());
    }
}
