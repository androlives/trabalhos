package aula.ada.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(String codigo, String mensagem, Instant timestamp, List<String> detalhes) {

    public static ErroResponse of(String codigo, String mensagem) {
        return new ErroResponse(codigo, mensagem, Instant.now(), null);
    }

    public static ErroResponse of(String codigo, String mensagem, List<String> detalhes) {
        return new ErroResponse(codigo, mensagem, Instant.now(), detalhes);
    }
}
