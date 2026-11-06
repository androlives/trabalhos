package aula.ada.platform.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class TarefaMetrics {

    private final Counter tarefasCriadas;

    @Inject
    public TarefaMetrics(MeterRegistry registry) {
        this.tarefasCriadas = Counter.builder("tarefas.criadas")
                .description("Quantidade de tarefas criadas com sucesso")
                .register(registry);
    }

    public void tarefaCriada() {
        tarefasCriadas.increment();
    }
}
