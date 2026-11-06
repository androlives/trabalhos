package aula.ada.platform.exception;

import aula.ada.dto.ErroResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class ErroGenericoMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(ErroGenericoMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException web) {
            int status = web.getResponse().getStatus();
            Response.Status conhecido = Response.Status.fromStatusCode(status);
            String motivo = conhecido != null ? conhecido.getReasonPhrase() : "Erro HTTP";
            String mensagem = status < 500 && web.getMessage() != null ? web.getMessage() : motivo;
            return montar(status, codigoPara(status), mensagem);
        }
        LOG.error("Erro inesperado", exception);
        return montar(500, "ERRO_INTERNO", "Erro interno do servidor");
    }

    private static String codigoPara(int status) {
        return switch (status) {
            case 400 -> "REQUISICAO_INVALIDA";
            case 404 -> "RECURSO_NAO_ENCONTRADO";
            case 405 -> "METODO_NAO_PERMITIDO";
            case 415 -> "MIDIA_NAO_SUPORTADA";
            default -> "ERRO_HTTP_" + status;
        };
    }

    private static Response montar(int status, String codigo, String mensagem) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(ErroResponse.of(codigo, mensagem))
                .build();
    }
}
