import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class MockController {

    private static Process mockProcess;
    private static final String MOCK_CLASS = "mock/PrioridadeMock.java";
    private static final int MOCK_PORT = 8089;

    public static void main(String[] args) throws Exception {
        int controlPort = args.length > 0 ? Integer.parseInt(args[0]) : 8090;
        HttpServer server = HttpServer.create(new InetSocketAddress(controlPort), 0);

        server.createContext("/mock/up", exchange -> {
            try {
                if (mockProcess != null && mockProcess.isAlive()) {
                    respond(exchange, 200, "{\"mock\":\"UP_ALREADY\",\"pid\":" + mockProcess.pid() + "}");
                    return;
                }
                String javaBin = Path.of(System.getProperty("java.home"), "bin", "java").toString();
                mockProcess = new ProcessBuilder(javaBin, MOCK_CLASS, String.valueOf(MOCK_PORT))
                        .inheritIO()
                        .start();
                TimeUnit.MILLISECONDS.sleep(800);
                respond(exchange, 200, "{\"mock\":\"UP\",\"pid\":" + mockProcess.pid() + "}");
            } catch (Exception e) {
                respond(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        server.createContext("/mock/down", exchange -> {
            try {
                if (mockProcess == null || !mockProcess.isAlive()) {
                    respond(exchange, 200, "{\"mock\":\"DOWN_ALREADY\"}");
                    return;
                }
                mockProcess.destroy();                         // mata o mock
                if (!mockProcess.waitFor(2, TimeUnit.SECONDS)) {
                    mockProcess.destroyForcibly();             // força
                }
                respond(exchange, 200, "{\"mock\":\"DOWN\"}");
            } catch (Exception e) {
                respond(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        server.createContext("/mock/status", exchange -> {
            boolean alive = mockProcess != null && mockProcess.isAlive();
            respond(exchange, 200, "{\"mock\":\"" + (alive ? "UP" : "DOWN") + "\"}");
        });

        server.start();
        System.out.println("Controlador ouvindo em " + controlPort);
        System.out.println("  PUT /mock/up     — liga o mock");
        System.out.println("  PUT /mock/down   — mata o mock (ConnectException)");
        System.out.println("  GET /mock/status — estado atual");
    }

    private static void respond(com.sun.net.httpserver.HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}