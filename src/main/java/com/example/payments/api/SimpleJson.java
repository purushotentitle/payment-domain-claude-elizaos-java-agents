package com.example.payments.api;

import com.example.payments.domain.ActionProposal;
import com.example.payments.domain.AgentResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class SimpleJson {
    private SimpleJson() {
    }

    public static Map<String, String> parseFlatObject(String json) {
        Map<String, String> values = new LinkedHashMap<>();
        if (json == null || json.isBlank()) {
            return values;
        }
        String trimmed = json.trim();
        if (trimmed.startsWith("{")) {
            trimmed = trimmed.substring(1);
        }
        if (trimmed.endsWith("}")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        for (String part : splitTopLevel(trimmed)) {
            int colon = part.indexOf(':');
            if (colon <= 0) {
                continue;
            }
            String key = unquote(part.substring(0, colon).trim());
            String rawValue = part.substring(colon + 1).trim();
            values.put(key, unquote(rawValue));
        }
        return values;
    }

    public static BigDecimal optionalDecimal(Map<String, String> values, String key) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            return null;
        }
        return new BigDecimal(value);
    }

    public static String responseToJson(AgentResponse response) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("agentId", response.agentId());
        payload.put("summary", response.summary());
        payload.put("recommendation", response.recommendation());
        payload.put("riskLevel", response.riskLevel());
        payload.put("requiresApproval", response.requiresApproval());
        payload.put("actions", response.actions().stream().map(SimpleJson::actionToMap).toList());
        payload.put("evidence", response.evidence());
        return stringify(payload);
    }

    public static String stringify(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String string) {
            return "\"" + escape(string) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Map<?, ?> map) {
            return map.entrySet().stream()
                    .map(entry -> stringify(String.valueOf(entry.getKey())) + ":" + stringify(entry.getValue()))
                    .collect(Collectors.joining(",", "{", "}"));
        }
        if (value instanceof List<?> list) {
            return list.stream().map(SimpleJson::stringify).collect(Collectors.joining(",", "[", "]"));
        }
        return stringify(String.valueOf(value));
    }

    public static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private static List<String> splitTopLevel(String input) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inString = false;
        boolean escaping = false;
        int depth = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (escaping) {
                current.append(c);
                escaping = false;
                continue;
            }
            if (c == '\\') {
                current.append(c);
                escaping = true;
                continue;
            }
            if (c == '"') {
                current.append(c);
                inString = !inString;
                continue;
            }
            if (!inString && (c == '{' || c == '[')) {
                depth++;
            } else if (!inString && (c == '}' || c == ']')) {
                depth--;
            } else if (!inString && depth == 0 && c == ',') {
                parts.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        if (!current.isEmpty()) {
            parts.add(current.toString());
        }
        return parts;
    }

    private static String unquote(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private static Map<String, Object> actionToMap(ActionProposal action) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", action.type());
        payload.put("description", action.description());
        payload.put("requiresApproval", action.requiresApproval());
        return payload;
    }
}
