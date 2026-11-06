package aula.ada.rest;

import aula.ada.dto.TarefaRequest;
import aula.ada.dto.TarefaResponse;
import aula.ada.service.TarefaService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;

@Path("/tarefas")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TarefaResource {
    @Inject
    TarefaService tarefaService;
    @Context
    UriInfo uriInfo;

    @GET
    public List<TarefaResponse> listar() {
        return tarefaService.listar();
    }

    @GET
    @Path("/{id}")
    public TarefaResponse buscar(@PathParam("id") long id) {
        return tarefaService.buscar(id);
    }

    @POST
    public Response criar(@Valid TarefaRequest request) {
        TarefaResponse criada = tarefaService.criar(request);
        URI location = uriInfo.getAbsolutePathBuilder().path(criada.getId().toString()).build();
        return Response.created(location).entity(criada).build();
    }

    @PUT
    @Path("/{id}")
    public TarefaResponse atualizar(@PathParam("id") long id, @Valid TarefaRequest request) {
        return tarefaService.atualizar(id, request);
    }

    @DELETE
    @Path("/{id}")
    public Response excluir(@PathParam("id") long id) {
        tarefaService.excluir(id);
        return Response.noContent().build();
    }
}
