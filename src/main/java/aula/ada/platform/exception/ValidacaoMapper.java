package aula.ada.platform.exception;

import aula.ada.dto.ErroResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

@Provider
public class ValidacaoMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<String> detalhes = exception.getConstraintViolations().stream()
                .map(ValidacaoMapper::formatar)
                .sorted()
                .toList();
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(ErroResponse.of("VALIDACAO_INVALIDA", "Dados da requisição inválidos", detalhes))
                .build();
    }

    private static String formatar(ConstraintViolation<?> violacao) {
        String campo = null;
        for (Path.Node no : violacao.getPropertyPath()) {
            campo = no.getName();
        }
        return campo + ": " + violacao.getMessage();
    }
}
