package aula.ada;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;


@QuarkusTest
class PrioridadeIntegracaoTest {

    @Test
    void servicoExternoForaDoArUsaFallbackMediaEAindaCriaTarefa() {
        given().contentType(ContentType.JSON).body("{\"titulo\":\"Estudar Quarkus\"}")
                .when().post("/tarefas")
                .then().statusCode(201)
                .body("prioridade", is("MEDIA"));
    }

    @Test
    void prioridadeInformadaNaRequisicaoEMantida() {
        given().contentType(ContentType.JSON).body("{\"titulo\":\"Estudar Quarkus\",\"prioridade\":\"ALTA\"}")
                .when().post("/tarefas")
                .then().statusCode(201)
                .body("prioridade", is("ALTA"));
    }
}
