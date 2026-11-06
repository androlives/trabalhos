package aula.ada.service;

public class TransicaoTarefaInvalidaException extends RuntimeException {
    public TransicaoTarefaInvalidaException(long id, Enum<?> statusAtual, Enum<?> novoStatus) {
        super("A tarefa " + id + " não pode ser alterada de " + statusAtual + " para " + novoStatus);
    }
}
