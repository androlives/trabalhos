package aula.ada.prioridades.boundary;

import aula.ada.prioridades.domain.OrigemPrioridade;
import aula.ada.prioridades.domain.PrioridadeResultado;
import aula.ada.prioridades.domain.PrioridadeService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/prioridades")
@Produces(MediaType.APPLICATION_JSON)
public class PrioridadeResource {

    private final PrioridadeService service;

    public PrioridadeResource(PrioridadeService service) {
        this.service = service;
    }

    @GET
    @Path("/sugestao")
    public Response sugerir(
            @QueryParam("titulo")
            @NotBlank(message = "O título não pode estar em branco")
            @Size(max = 100, message = "O título deve possuir no máximo 100 caracteres") String titulo) {
        PrioridadeResultado resultado = service.sugerir(titulo);
        PrioridadeResponse resposta = PrioridadeResponse.from(resultado);
        if (resultado.origem() == OrigemPrioridade.FALLBACK) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(resposta).build();
        }
        return Response.ok(resposta).build();
    }
}
