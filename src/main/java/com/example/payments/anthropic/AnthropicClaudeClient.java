package com.example.payments.anthropic;

import com.example.payments.api.SimpleJson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class AnthropicClaudeClient implements ClaudeClient {
    private static final URI MESSAGES_API = URI.create("https://api.anthropic.com/v1/messages");

    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;

    public AnthropicClaudeClient(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) throws IOException, InterruptedException {
        String body = """
                {
                  "model": "%s",
                  "max_tokens": 700,
                  "temperature": 0.2,
                  "system": "%s",
                  "messages": [
                    {"role": "user", "content": "%s"}
                  ]
                }
                """.formatted(
                SimpleJson.escape(model),
                SimpleJson.escape(systemPrompt),
                SimpleJson.escape(userPrompt)
        );

        HttpRequest request = HttpRequest.newBuilder(MESSAGES_API)
                .timeout(Duration.ofSeconds(40))
                .header("content-type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Anthropic API failed with status " + response.statusCode() + ": " + response.body());
        }
        return extractFirstTextBlock(response.body());
    }

    private String extractFirstTextBlock(String json) {
        int textKey = json.indexOf("\"text\"");
        if (textKey < 0) {
            return json;
        }
        int colon = json.indexOf(':', textKey);
        int firstQuote = json.indexOf('"', colon + 1);
        if (colon < 0 || firstQuote < 0) {
            return json;
        }
        StringBuilder value = new StringBuilder();
        boolean escaping = false;
        for (int i = firstQuote + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escaping) {
                value.append(switch (c) {
                    case 'n' -> '\n';
                    case 'r' -> '\r';
                    case 't' -> '\t';
                    case '"' -> '"';
                    case '\\' -> '\\';
                    default -> c;
                });
                escaping = false;
            } else if (c == '\\') {
                escaping = true;
            } else if (c == '"') {
                return value.toString();
            } else {
                value.append(c);
            }
        }
        return value.toString();
    }
}
