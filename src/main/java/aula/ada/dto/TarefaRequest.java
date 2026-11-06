package aula.ada.dto;

import aula.ada.domain.PrioridadeTarefa;
import aula.ada.domain.StatusTarefa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarefaRequest {
    @NotBlank(message = "O título não pode estar em branco")
    @Size(min = 3, max = 100, message = "O título deve possuir entre 3 e 100 caracteres")
    private String titulo;
    @Size(max = 255, message = "A descrição deve possuir no máximo 255 caracteres")
    private String descricao;
    private StatusTarefa status;
    private PrioridadeTarefa prioridade;
}
