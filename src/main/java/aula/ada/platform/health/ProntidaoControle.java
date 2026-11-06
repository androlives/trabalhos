package aula.ada.platform.health;

import jakarta.inject.Singleton;

import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
public class ProntidaoControle {

    private final AtomicBoolean pronta = new AtomicBoolean(true);

    public boolean isPronta() {
        return pronta.get();
    }

    public void ativar() {
        pronta.set(true);
    }

    public void desativar() {
        pronta.set(false);
    }
}
