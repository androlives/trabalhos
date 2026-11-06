package aula.ada.platform.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;


@Readiness
@ApplicationScoped
public class ProntidaoReadinessCheck implements HealthCheck {

    @Inject
    ProntidaoControle controle;

    @Override
    public HealthCheckResponse call() {
        if (controle.isPronta()) {
            return HealthCheckResponse.up("prontidao-controlavel");
        }
        return HealthCheckResponse.named("prontidao-controlavel")
                .down()
                .withData("motivo", "desativada manualmente via /admin/readiness/down")
                .build();
    }
}
