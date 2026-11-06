package aula.ada.prioridades.domain;

import aula.ada.domain.PrioridadeTarefa;
import aula.ada.prioridades.integration.prioridade.PrioridadeExterna;
import aula.ada.prioridades.integration.prioridade.PrioridadeGateway;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/** Consulta a prioridade externa sem alterar tarefas ou repetir escritas no banco. */
@ApplicationScoped
public class PrioridadeService {

    private final PrioridadeGateway gateway;

    public PrioridadeService(@RestClient PrioridadeGateway gateway) {
        this.gateway = gateway;
    }

    @Retry(maxRetries = 2, delay = 100, jitter = 0)
    @Fallback(fallbackMethod = "fallbackSugerir")
    public PrioridadeResultado sugerir(String titulo) {
        PrioridadeExterna resposta = gateway.consultar(titulo);
        if (resposta == null || resposta.prioridade() == null) {
            throw new IllegalStateException("O serviço externo não informou uma prioridade");
        }
        PrioridadeTarefa prioridade = PrioridadeTarefa.valueOf(resposta.prioridade());
        return new PrioridadeResultado(prioridade, OrigemPrioridade.SERVICO_EXTERNO);
    }

    public PrioridadeResultado fallbackSugerir(String titulo) {
        return new PrioridadeResultado(PrioridadeTarefa.MEDIA, OrigemPrioridade.FALLBACK);
    }
}
