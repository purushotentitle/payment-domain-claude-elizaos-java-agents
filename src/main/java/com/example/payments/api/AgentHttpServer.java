package com.example.payments.api;

import com.example.payments.agents.PaymentAgent;
import com.example.payments.domain.AgentContext;
import com.example.payments.domain.AgentRequest;
import com.example.payments.domain.AgentResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class AgentHttpServer {
    private final int port;
    private final List<PaymentAgent> agents;
    private final AgentContext context;

    public AgentHttpServer(int port, List<PaymentAgent> agents, AgentContext context) {
        this.port = port;
        this.agents = agents;
        this.context = context;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/health", this::handleHealth);
        server.createContext("/agents", this::handleAgents);
        server.start();
    }

    private void handleHealth(HttpExchange exchange) throws IOException {
        writeJson(exchange, 200, "{\"status\":\"ok\"}");
    }

    private void handleAgents(HttpExchange exchange) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            List<Map<String, String>> payload = agents.stream()
                    .map(agent -> Map.of(
                            "id", agent.id(),
                            "name", agent.name(),
                            "description", agent.description()
                    ))
                    .toList();
            writeJson(exchange, 200, SimpleJson.stringify(payload));
            return;
        }

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeJson(exchange, 405, "{\"error\":\"method_not_allowed\"}");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        if (parts.length != 4 || !"invoke".equals(parts[3])) {
            writeJson(exchange, 404, "{\"error\":\"expected /agents/{agentId}/invoke\"}");
            return;
        }

        String agentId = parts[2];
        Optional<PaymentAgent> selected = agents.stream().filter(agent -> agent.id().equals(agentId)).findFirst();
        if (selected.isEmpty()) {
            writeJson(exchange, 404, "{\"error\":\"unknown_agent\"}");
            return;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        AgentRequest request = toAgentRequest(SimpleJson.parseFlatObject(body));
        AgentResponse response = selected.get().handle(request, context);
        writeJson(exchange, 200, SimpleJson.responseToJson(response));
    }

    private AgentRequest toAgentRequest(Map<String, String> values) {
        BigDecimal amount = SimpleJson.optionalDecimal(values, "amount");
        Map<String, String> metadata = new LinkedHashMap<>(values);
        metadata.keySet().removeAll(List.of("message", "paymentId", "amount", "currency", "merchantId", "customerId"));
        return new AgentRequest(
                values.getOrDefault("message", ""),
                values.get("paymentId"),
                amount,
                values.getOrDefault("currency", "USD"),
                values.get("merchantId"),
                values.get("customerId"),
                metadata
        );
    }

    private void writeJson(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("content-type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
