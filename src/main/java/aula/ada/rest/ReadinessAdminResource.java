package aula.ada.rest;

import aula.ada.platform.health.ProntidaoControle;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.Map;

@Path("/admin/readiness")
@Produces(MediaType.APPLICATION_JSON)
public class ReadinessAdminResource {

    @Inject
    ProntidaoControle controle;

    @GET
    public Map<String, Object> estado() {
        return Map.of("pronta", controle.isPronta());
    }

    @PUT
    @Path("/{estado}")
    public Map<String, Object> alterar(@PathParam("estado") String estado) {
        switch (estado.toLowerCase()) {
            case "up" -> controle.ativar();
            case "down" -> controle.desativar();
            default -> throw new BadRequestException("Estado inválido: use 'up' ou 'down'");
        }
        return Map.of("pronta", controle.isPronta());
    }
}
