package aula.ada.repository;

import aula.ada.domain.StatusTarefa;
import aula.ada.entity.TarefaEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class TarefaRepository implements PanacheRepository<TarefaEntity> {

    public List<TarefaEntity> findByStatus(StatusTarefa status) {
        return list("status = ?1", status);
    }
}
