package aula.ada.platform.exception;

import aula.ada.service.TarefaNaoEncontradaException;
import aula.ada.dto.ErroResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class TarefaNaoEncontradaMapper implements ExceptionMapper<TarefaNaoEncontradaException> {

    @Override
    public Response toResponse(TarefaNaoEncontradaException exception) {
        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(ErroResponse.of("TAREFA_NAO_ENCONTRADA", exception.getMessage()))
                .build();
    }
}