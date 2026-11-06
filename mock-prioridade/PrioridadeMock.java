import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/** Mock HTTP local: java mock-prioridade/PrioridadeMock.java [porta] [falhasIniciais]. */
public class PrioridadeMock {
    public static void main(String[] args) throws IOException {
        int porta = args.length > 0 ? Integer.parseInt(args[0]) : 8081;
        int falhasIniciais = args.length > 1 ? Integer.parseInt(args[1]) : 0;
        if (porta < 1 || porta > 65535 || falhasIniciais < 0) {
            throw new IllegalArgumentException("Use porta de 1 a 65535 e falhasIniciais >= 0");
        }
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", porta), 0);
        var executor = Executors.newFixedThreadPool(4);
        var chamadas = new AtomicInteger();
        server.setExecutor(executor);
        server.createContext("/prioridade", exchange -> {
            try (exchange) {
                if (!"/prioridade".equals(exchange.getRequestURI().getPath())) {
                    responder(exchange, 404, "{\"mensagem\":\"Rota inexistente\"}");
                    return;
                }
                if (!"GET".equals(exchange.getRequestMethod())) {
                    exchange.getResponseHeaders().set("Allow", "GET");
                    responder(exchange, 405, "{\"mensagem\":\"Use GET\"}");
                    return;
                }
                String titulo = titulo(exchange);
                if (titulo == null || titulo.isBlank() || titulo.length() > 100) {
                    responder(exchange, 400, "{\"mensagem\":\"Informe titulo de 1 a 100 caracteres\"}");
                    return;
                }
                int chamada = chamadas.incrementAndGet();
                if (chamada <= falhasIniciais) {
                    System.out.printf("Chamada %d -> 503 (falha simulada)%n", chamada);
                    responder(exchange, 503, "{\"mensagem\":\"Indisponibilidade simulada\"}");
                    return;
                }
                String texto = titulo.toLowerCase(Locale.ROOT);
                String prioridade = texto.contains("urgente") || texto.contains("critico")
                        || texto.contains("crítico") ? "ALTA"
                        : texto.contains("opcional") ? "BAIXA" : "MEDIA";
                System.out.printf("Chamada %d -> 200 (%s)%n", chamada, prioridade);
                responder(exchange, 200, "{\"prioridade\":\"" + prioridade + "\"}");
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop(0);
            executor.shutdownNow();
        }));
        server.start();
        System.out.printf("Mock de prioridade na porta %d. Falhas iniciais: %d. Ctrl+C encerra.%n",
                porta, falhasIniciais);
    }

    private static String titulo(HttpExchange exchange) {
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null) {
            return null;
        }
        for (String parametro : query.split("&")) {
            String[] partes = parametro.split("=", 2);
            if (partes.length == 2 && "titulo".equals(partes[0])) {
                return URLDecoder.decode(partes[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private static void responder(HttpExchange exchange, int status, String json) throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
    }
}
