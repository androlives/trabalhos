package aula.ada.client;

import aula.ada.domain.PrioridadeTarefa;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

@ApplicationScoped
public class PrioridadeGateway {

    private static final Logger LOG = Logger.getLogger(PrioridadeGateway.class);

    @Inject
    @RestClient
    PrioridadeClient client;

    @Retry(maxRetries = 2, delay = 200)
    @Timeout(3000)
    @Fallback(fallbackMethod = "prioridadePadrao")
    public PrioridadeTarefa sugerir(String titulo) {
        LOG.infof("Consultando serviço de prioridade para a tarefa '%s'", titulo);
        PrioridadeSugeridaResponse resposta = client.sugerir(titulo);
        if (resposta == null || resposta.prioridade() == null) {
            throw new IllegalStateException("Resposta inválida do serviço de prioridade");
        }
        return resposta.prioridade();
    }

    PrioridadeTarefa prioridadePadrao(String titulo) {
        LOG.warnf("Serviço de prioridade indisponível; usando fallback %s para '%s'",
                PrioridadeTarefa.MEDIA, titulo);
        return PrioridadeTarefa.MEDIA;
    }
}
