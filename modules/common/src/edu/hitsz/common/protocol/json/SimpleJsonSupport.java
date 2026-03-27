package edu.hitsz.common.protocol.json;

import java.util.ArrayList;
import java.util.List;

final class SimpleJsonSupport {

    private SimpleJsonSupport() {
    }

    static String quote(String value) {
        if (value == null) {
            return "null";
        }
        String escaped = value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
        return "\"" + escaped + "\"";
    }

    static String unquote(String value) {
        if (value == null || "null".equals(value)) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() < 2 || trimmed.charAt(0) != '"' || trimmed.charAt(trimmed.length() - 1) != '"') {
            return trimmed;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < trimmed.length() - 1; i++) {
            char current = trimmed.charAt(i);
            if (current == '\\' && i + 1 < trimmed.length() - 1) {
                char escaped = trimmed.charAt(++i);
                switch (escaped) {
                    case '"':
                        builder.append('"');
                        break;
                    case '\\':
                        builder.append('\\');
                        break;
                    case 'n':
                        builder.append('\n');
                        break;
                    case 'r':
                        builder.append('\r');
                        break;
                    case 't':
                        builder.append('\t');
                        break;
                    default:
                        builder.append(escaped);
                        break;
                }
                continue;
            }
            builder.append(current);
        }
        return builder.toString();
    }

    static String extractString(String json, String key) {
        return unquote(extractJsonValue(json, key));
    }

    static String extractStringOrDefault(String json, String key, String defaultValue) {
        if (!containsKey(json, key)) {
            return defaultValue;
        }
        return extractString(json, key);
    }

    static long extractLong(String json, String key) {
        return Long.parseLong(extractJsonValue(json, key));
    }

    static long extractLongOrDefault(String json, String key, long defaultValue) {
        if (!containsKey(json, key)) {
            return defaultValue;
        }
        return extractLong(json, key);
    }

    static int extractInt(String json, String key) {
        return Integer.parseInt(extractJsonValue(json, key));
    }

    static int extractIntOrDefault(String json, String key, int defaultValue) {
        if (!containsKey(json, key)) {
            return defaultValue;
        }
        return extractInt(json, key);
    }

    static boolean extractBoolean(String json, String key) {
        return Boolean.parseBoolean(extractJsonValue(json, key));
    }

    static boolean extractBooleanOrDefault(String json, String key, boolean defaultValue) {
        if (!containsKey(json, key)) {
            return defaultValue;
        }
        return extractBoolean(json, key);
    }

    static String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"";
        int keyIndex = json.indexOf(pattern);
        if (keyIndex < 0) {
            throw new IllegalArgumentException("Missing key: " + key);
        }

        int colonIndex = json.indexOf(':', keyIndex + pattern.length());
        if (colonIndex < 0) {
            throw new IllegalArgumentException("Missing colon for key: " + key);
        }

        int valueStart = skipWhitespace(json, colonIndex + 1);
        int valueEnd = findValueEnd(json, valueStart);
        return json.substring(valueStart, valueEnd).trim();
    }

    static String extractJsonValueOrDefault(String json, String key, String defaultValue) {
        if (!containsKey(json, key)) {
            return defaultValue;
        }
        return extractJsonValue(json, key);
    }

    static boolean containsKey(String json, String key) {
        return json.indexOf("\"" + key + "\"") >= 0;
    }

    static List<String> splitTopLevelArray(String jsonArray) {
        String trimmed = jsonArray.trim();
        if ("[]".equals(trimmed)) {
            return new ArrayList<>();
        }
        if (trimmed.length() < 2 || trimmed.charAt(0) != '[' || trimmed.charAt(trimmed.length() - 1) != ']') {
            throw new IllegalArgumentException("Not a JSON array: " + jsonArray);
        }

        List<String> values = new ArrayList<>();
        int index = 1;
        while (index < trimmed.length() - 1) {
            index = skipWhitespace(trimmed, index);
            if (index >= trimmed.length() - 1) {
                break;
            }
            if (trimmed.charAt(index) == ',') {
                index++;
                continue;
            }
            int valueEnd = findValueEnd(trimmed, index);
            values.add(trimmed.substring(index, valueEnd).trim());
            index = valueEnd + 1;
        }
        return values;
    }

    static String normalizePayload(String payload) {
        if (payload == null) {
            return "null";
        }
        String trimmed = payload.trim();
        if (trimmed.isEmpty()) {
            return quote("");
        }
        char first = trimmed.charAt(0);
        if (first == '{' || first == '[' || first == '"' || first == '-' || Character.isDigit(first)) {
            return trimmed;
        }
        if ("true".equals(trimmed) || "false".equals(trimmed) || "null".equals(trimmed)) {
            return trimmed;
        }
        return quote(trimmed);
    }

    private static int skipWhitespace(String value, int index) {
        int cursor = index;
        while (cursor < value.length() && Character.isWhitespace(value.charAt(cursor))) {
            cursor++;
        }
        return cursor;
    }

    private static int findValueEnd(String value, int start) {
        char first = value.charAt(start);
        if (first == '"') {
            return findStringEnd(value, start);
        }
        if (first == '{' || first == '[') {
            return findContainerEnd(value, start);
        }

        int cursor = start;
        while (cursor < value.length()) {
            char current = value.charAt(cursor);
            if (current == ',' || current == '}' || current == ']') {
                break;
            }
            cursor++;
        }
        return cursor;
    }

    private static int findStringEnd(String value, int start) {
        int cursor = start + 1;
        while (cursor < value.length()) {
            char current = value.charAt(cursor);
            if (current == '\\') {
                cursor += 2;
                continue;
            }
            if (current == '"') {
                return cursor + 1;
            }
            cursor++;
        }
        throw new IllegalArgumentException("Unterminated JSON string: " + value);
    }

    private static int findContainerEnd(String value, int start) {
        char open = value.charAt(start);
        char close = open == '{' ? '}' : ']';
        int depth = 0;
        boolean inString = false;

        for (int cursor = start; cursor < value.length(); cursor++) {
            char current = value.charAt(cursor);
            if (current == '"' && (cursor == start || value.charAt(cursor - 1) != '\\')) {
                inString = !inString;
            }
            if (inString) {
                continue;
            }
            if (current == open) {
                depth++;
            } else if (current == close) {
                depth--;
                if (depth == 0) {
                    return cursor + 1;
                }
            }
        }
        throw new IllegalArgumentException("Unterminated JSON container: " + value);
    }
}
