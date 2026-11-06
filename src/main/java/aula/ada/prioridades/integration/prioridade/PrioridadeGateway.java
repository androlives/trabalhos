package aula.ada.prioridades.integration.prioridade;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/prioridade")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "prioridade-local")
public interface PrioridadeGateway {

    @GET
    PrioridadeExterna consultar(@QueryParam("titulo") String titulo);
}
