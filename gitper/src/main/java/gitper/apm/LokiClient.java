package gitper.apm;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

public class LokiClient {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String application;
    private final String lokiUrl;
    
    /**
     * @param application "minerva"
     * @param lokiHost "http://grafana:3100"
     */
    public LokiClient(String application, String lokiHost) {
        this.application = application;
        if (!lokiHost.startsWith("http://") && !lokiHost.startsWith("https://")) {
            throw new IllegalArgumentException("lokiHost must start with http:// or https://");
        } else if (lokiHost.endsWith("/")) {
            throw new IllegalArgumentException("lokiHost must not end with '/'");
        }
        this.lokiUrl = lokiHost + "/loki/api/v1/push";
    }

    public void log(String level, String message) {
        long nanos = Instant.now().getEpochSecond() * 1_000_000_000L + Instant.now().getNano();
        String json = """
                {
                  "streams": [
                    {
                      "stream": {
                        "application": "%s",
                        "level": "%s"
                      },
                      "values": [
                        [
                          "%d",
                          "%s"
                        ]
                      ]
                    }
                  ]
                }
                """.formatted(
                        application,
                        esc(level),
                        nanos,
                        esc(message));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(lokiUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        try {
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() >= 300) {
                System.err.println("[LokiClient] Loki returned HTTP " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("[LokiClient] Could not send log to Loki: " + e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static String esc(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}