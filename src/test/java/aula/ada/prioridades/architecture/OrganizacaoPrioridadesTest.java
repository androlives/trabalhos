package aula.ada.prioridades.architecture;

import aula.ada.prioridades.boundary.PrioridadeResource;
import aula.ada.prioridades.domain.PrioridadeResultado;
import aula.ada.prioridades.domain.PrioridadeService;
import aula.ada.prioridades.integration.prioridade.PrioridadeGateway;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrganizacaoPrioridadesTest {
    @Test
    void organizaFeatureComBoundaryDomainEIntegration() {
        assertEquals("aula.ada.prioridades.boundary", PrioridadeResource.class.getPackageName());
        assertEquals("aula.ada.prioridades.domain", PrioridadeService.class.getPackageName());
        assertEquals("aula.ada.prioridades.integration.prioridade", PrioridadeGateway.class.getPackageName());
    }

    @Test
    void servicoRetornaResultadoDeDominioEmVezDeRespostaHttp() throws NoSuchMethodException {
        assertEquals(PrioridadeResultado.class,
                PrioridadeService.class.getMethod("sugerir", String.class).getReturnType());
    }
}
