package aula.ada.prioridades.boundary;

import aula.ada.domain.PrioridadeTarefa;
import aula.ada.prioridades.domain.OrigemPrioridade;
import aula.ada.prioridades.domain.PrioridadeResultado;

/** Contrato HTTP separado dos modelos de domínio e da integração externa. */
public record PrioridadeResponse(PrioridadeTarefa prioridade, OrigemPrioridade origem) {

    public static PrioridadeResponse from(PrioridadeResultado resultado) {
        return new PrioridadeResponse(resultado.prioridade(), resultado.origem());
    }
}
