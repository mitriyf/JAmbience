package ru.mitriyf.jambience.utils.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Json {
    private final String src;
    private int pos;

    public Json(String src) {
        this.src = src;
    }

    public Object parseValue() {
        skipWs();
        char c = src.charAt(pos);
        switch (c) {
            case '{': {
                return parseObject();
            }
            case '[': {
                return parseArray();
            }
            case '"': {
                return parseString();
            }
            case 't': {
                pos += 4;
                return true;
            }
            case 'f': {
                pos += 5;
                return false;
            }
            case 'n': {
                pos += 4;
                return null;
            }
            default: {
                return parseNumber();
            }
        }
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> map = new LinkedHashMap<>();
        pos++;
        skipWs();
        if (src.charAt(pos) == '}') {
            pos++;
            return map;
        }
        while (true) {
            skipWs();
            String key = parseString();
            skipWs();
            pos++;
            map.put(key, parseValue());
            skipWs();
            char c = src.charAt(pos++);
            if (c == '}') {
                break;
            }
        }
        return map;
    }

    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();
        pos++;
        skipWs();
        if (src.charAt(pos) == ']') {
            pos++;
            return list;
        }
        while (true) {
            list.add(parseValue());
            skipWs();
            char c = src.charAt(pos++);
            if (c == ']') {
                break;
            }
        }
        return list;
    }

    private String parseString() {
        pos++;
        StringBuilder stringBuilder = new StringBuilder();
        while (true) {
            char c = src.charAt(pos++);
            if (c == '"') {
                break;
            }
            if (c == '\\') {
                char e = src.charAt(pos++);
                findChar(stringBuilder, e);
            } else {
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }

    private void findChar(StringBuilder stringBuilder, char e) {
        switch (e) {
            case 'n': {
                stringBuilder.append('\n');
                break;
            }
            case 't': {
                stringBuilder.append('\t');
                break;
            }
            case 'r': {
                stringBuilder.append('\r');
                break;
            }
            case 'b': {
                stringBuilder.append('\b');
                break;
            }
            case 'f': {
                stringBuilder.append('\f');
                break;
            }
            case '/': {
                stringBuilder.append('/');
                break;
            }
            case '"': {
                stringBuilder.append('"');
                break;
            }
            case '\\': {
                stringBuilder.append('\\');
                break;
            }
            case 'u': {
                stringBuilder.append((char) Integer.parseInt(src.substring(pos, pos + 4), 16));
                pos += 4;
                break;
            }
            default: {
                stringBuilder.append(e);
            }
        }
    }

    private Number parseNumber() {
        int start = pos;
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (c == ',' || c == '}' || c == ']' || Character.isWhitespace(c)) {
                break;
            }
            pos++;
        }
        String number = src.substring(start, pos);
        if (number.contains(".") || number.contains("e") || number.contains("E")) {
            return Double.parseDouble(number);
        }
        return Long.parseLong(number);
    }

    public void skipWs() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
            pos++;
        }
    }
}
