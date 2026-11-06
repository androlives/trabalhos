package aula.ada.platform.exception;

import aula.ada.dto.ErroResponse;

import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider

@Priority(Priorities.USER - 1)
public class CorpoInvalidoMapper implements ExceptionMapper<JsonMappingException> {


    @Override
    public Response toResponse(JsonMappingException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(ErroResponse.of("CORPO_INVALIDO",
                        "O corpo da requisição é inválido ou contém valores não aceitos"))
                .build();
    }
}
