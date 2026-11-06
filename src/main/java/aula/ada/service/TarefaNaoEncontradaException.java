package aula.ada.service;

public class TarefaNaoEncontradaException extends RuntimeException {
    public TarefaNaoEncontradaException(long id) {
        super("Tarefa não encontrada com ID: " + id);
    }
}
