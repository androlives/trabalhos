package aula.ada.prioridades.domain;

import aula.ada.domain.PrioridadeTarefa;

/** Resultado de negócio independente do contrato HTTP da aplicação. */
public record PrioridadeResultado(PrioridadeTarefa prioridade, OrigemPrioridade origem) {
}
