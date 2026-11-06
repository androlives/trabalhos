package aula.ada.entity;

import aula.ada.domain.PrioridadeTarefa;
import aula.ada.domain.StatusTarefa;
import aula.ada.dto.TarefaRequest;
import aula.ada.dto.TarefaResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tarefas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarefaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(length = 255)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTarefa status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadeTarefa prioridade;

    public static TarefaEntity from(TarefaRequest request) {
        return TarefaEntity.builder()
                .titulo(request.getTitulo())
                .descricao(request.getDescricao())
                .status(request.getStatus() == null ? StatusTarefa.ABERTA : request.getStatus())
                .prioridade(request.getPrioridade() == null ? PrioridadeTarefa.MEDIA : request.getPrioridade())
                .build();
    }

    public TarefaResponse toResponse() {
        return TarefaResponse.builder()
                .id(this.id)
                .titulo(this.titulo)
                .descricao(this.descricao)
                .status(this.status)
                .prioridade(this.prioridade)
                .build();
    }
}
