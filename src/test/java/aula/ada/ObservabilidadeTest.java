package aula.ada;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class ObservabilidadeTest {

    @Test
    void readinessFicaDownQuandoDesativadoELivenessContinuaUp() {
        try {
            given().when().put("/admin/readiness/down").then().statusCode(200);
            given().when().get("/q/health/ready").then().statusCode(503);
            given().when().get("/q/health/live").then().statusCode(200);
        } finally {
            given().when().put("/admin/readiness/up").then().statusCode(200);
        }
        given().when().get("/q/health/ready").then().statusCode(200);
    }

    @Test
    void contadorDeTarefasCriadasAumentaAposCriarTarefa() {
        double antes = lerContador();

        given().contentType(ContentType.JSON).body("{\"titulo\":\"Medir criação\",\"prioridade\":\"ALTA\"}")
                .when().post("/tarefas").then().statusCode(201);

        assertEquals(antes + 1, lerContador(), 0.001);
    }

    private double lerContador() {
        String corpo = given().when().get("/q/metrics").then().statusCode(200).extract().asString();
        Matcher m = Pattern.compile("(?m)^tarefas_criadas_total(?:\\{[^}]*})?\\s+([0-9.eE+-]+)").matcher(corpo);
        return m.find() ? Double.parseDouble(m.group(1)) : 0.0;
    }
}
