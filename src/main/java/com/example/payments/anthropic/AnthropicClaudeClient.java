package com.example.payments.anthropic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public final class AnthropicClaudeClient implements ClaudeClient {
    private static final URI MESSAGES_API = URI.create("https://api.anthropic.com/v1/messages");

    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public AnthropicClaudeClient(String apiKey, String model, ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) throws IOException, InterruptedException {
        Map<String, Object> payload = Map.of(
                "model", model,
                "max_tokens", 700,
                "temperature", 0.2,
                "system", systemPrompt,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", userPrompt
                ))
        );

        HttpRequest request = HttpRequest.newBuilder(MESSAGES_API)
                .timeout(Duration.ofSeconds(40))
                .header("content-type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Anthropic API failed with status " + response.statusCode() + ": " + response.body());
        }

        AnthropicMessageResponse parsed = objectMapper.readValue(response.body(), AnthropicMessageResponse.class);
        if (parsed.content() == null || parsed.content().isEmpty()) {
            return response.body();
        }

        return parsed.content().stream()
                .filter(block -> "text".equals(block.type()))
                .map(AnthropicContentBlock::text)
                .findFirst()
                .orElse(response.body());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AnthropicMessageResponse(List<AnthropicContentBlock> content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record AnthropicContentBlock(String type, String text) {
    }
}
