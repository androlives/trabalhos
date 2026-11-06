package aula.ada.client;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "prioridade-api")
@Path("/prioridades")
public interface PrioridadeClient {

    @GET
    @Path("/sugestao")
    @Produces(MediaType.APPLICATION_JSON)
    PrioridadeSugeridaResponse sugerir(@QueryParam("titulo") String titulo);
}
