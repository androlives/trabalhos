package aula.ada.platform.exception;

import aula.ada.dto.ErroResponse;
import aula.ada.service.TransicaoTarefaInvalidaException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class TransicaoTarefaInvalidaMapper implements ExceptionMapper<TransicaoTarefaInvalidaException> {

    @Override
    public Response toResponse(TransicaoTarefaInvalidaException exception) {
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(ErroResponse.of("TRANSICAO_TAREFA_INVALIDA", exception.getMessage()))
                .build();
    }
}
