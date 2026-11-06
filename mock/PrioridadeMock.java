import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Mock HTTP local do serviço de prioridade. Sem dependências: java mock/PrioridadeMock.java [porta]
 * Regra: título com "urgente" -> ALTA; com "depois" -> BAIXA; qualquer outro -> MEDIA.
 * Para simular o serviço DOWN, basta encerrar o processo (Ctrl+C).
 */
public class PrioridadeMock {

    public static void main(String[] args) throws Exception {
        int porta = args.length > 0 ? Integer.parseInt(args[0]) : 8089;
        HttpServer servidor = HttpServer.create(new InetSocketAddress(porta), 0);

        servidor.createContext("/prioridades/sugestao", troca -> {
            String titulo = "";
            String query = troca.getRequestURI().getRawQuery();
            if (query != null) {
                for (String par : query.split("&")) {
                    if (par.startsWith("titulo=")) {
                        titulo = URLDecoder.decode(par.substring(7), StandardCharsets.UTF_8).toLowerCase();
                    }
                }
            }
            String prioridade = titulo.contains("urgente") ? "ALTA" : titulo.contains("depois") ? "BAIXA" : "MEDIA";
            byte[] corpo = ("{\"prioridade\":\"" + prioridade + "\",\"origem\":\"mock\"}").getBytes(StandardCharsets.UTF_8);
            troca.getResponseHeaders().add("Content-Type", "application/json");
            troca.sendResponseHeaders(200, corpo.length);
            troca.getResponseBody().write(corpo);
            troca.close();
            System.out.println("[mock] titulo='" + titulo + "' -> " + prioridade);
        });

        servidor.start();
        System.out.println("Mock de prioridade ouvindo na porta " + porta);
    }
}
