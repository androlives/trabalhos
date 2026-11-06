package aula.ada.prioridades.boundary;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/** Serviço HTTP real, isolado e controlável pelos testes, sem dependência externa. */
public class PrioridadeTestResource implements QuarkusTestResourceLifecycleManager {
    private final AtomicReference<String> cenario = new AtomicReference<>("SUCESSO");
    private final AtomicInteger chamadas = new AtomicInteger();
    private final AtomicReference<String> ultimoTitulo = new AtomicReference<>("");
    private HttpServer server;
    private ExecutorService executor;

    @Override
    public Map<String, String> start() {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            executor = Executors.newCachedThreadPool(runnable -> {
                Thread thread = new Thread(runnable, "mock-prioridade-test");
                thread.setDaemon(true);
                return thread;
            });
            server.setExecutor(executor);
            server.createContext("/prioridade", this::consultar);
            server.createContext("/controle", this::controlar);
            server.start();
            return Map.of(
                    "quarkus.rest-client.prioridade-local.url", "http://127.0.0.1:" + server.getAddress().getPort(),
                    "quarkus.rest-client.prioridade-local.connect-timeout", "150",
                    "quarkus.rest-client.prioridade-local.read-timeout", "200");
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível iniciar o mock de prioridade", exception);
        }
    }

    private void consultar(HttpExchange exchange) throws IOException {
        int tentativa = chamadas.incrementAndGet();
        ultimoTitulo.set(parametro(exchange, "titulo"));
        String atual = cenario.get();
        if ("LENTO".equals(atual)) {
            try {
                Thread.sleep(700);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                exchange.close();
                return;
            }
        }
        if ("INDISPONIVEL".equals(atual) || ("DUAS_FALHAS".equals(atual) && tentativa <= 2)) {
            responder(exchange, 503, "{\"mensagem\":\"Serviço indisponível\"}");
        } else if ("INVALIDO".equals(atual)) {
            responder(exchange, 200, "{\"prioridade\":\"URGENTE\"}");
        } else {
            responder(exchange, 200, "{\"prioridade\":\"ALTA\"}");
        }
    }

    private void controlar(HttpExchange exchange) throws IOException {
        String novoCenario = parametro(exchange, "cenario");
        if (novoCenario != null) {
            cenario.set(novoCenario);
            chamadas.set(0);
            ultimoTitulo.set("");
        }
        String titulo = ultimoTitulo.get().replace("\\", "\\\\").replace("\"", "\\\"");
        responder(exchange, 200, "{\"chamadas\":" + chamadas.get() + ",\"titulo\":\"" + titulo + "\"}");
    }

    private static String parametro(HttpExchange exchange, String nome) {
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null) {
            return null;
        }
        return Arrays.stream(query.split("&"))
                .map(par -> par.split("=", 2))
                .filter(par -> URLDecoder.decode(par[0], StandardCharsets.UTF_8).equals(nome))
                .map(par -> par.length > 1 ? URLDecoder.decode(par[1], StandardCharsets.UTF_8) : "")
                .findFirst().orElse(null);
    }

    private static void responder(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        try (exchange) {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
        } catch (IOException exception) {
            // O cliente fecha a conexão nos testes de timeout.
            exchange.close();
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop(0);
        }
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
