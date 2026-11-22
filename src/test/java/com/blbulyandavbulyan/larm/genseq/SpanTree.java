package com.blbulyandavbulyan.larm.genseq;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.sdk.trace.data.SpanData;

/**
 * Turns a flat list of spans into one ordered {@link Call} tree per HTTP request.
 */
public final class SpanTree {
    private static final String METHOD_KEY = "method";
    private static final String STATUS_CODE_KEY = "http.response.status_code";
    private static final String HTTP_URL_KEY = "http.url";
    private static final String JDBC_PREFIX = "jdbc.query[";
    private static final String CHAT_CLIENT_PREFIX = "spring.ai.chat.client";
    private static final String AI_KIND_KEY = "spring.ai.kind";
    private static final String OPERATION_KEY = "gen_ai.operation.name";
    private static final String CONTROLLER_SUFFIX = "Controller";
    private static final String COUNT = "count";

    private final Map<String, List<SpanData>> childrenByParent = new HashMap<>();

    private SpanTree(List<SpanData> spans) {
        for (SpanData span : spans) {
            childrenByParent.computeIfAbsent(span.getParentSpanId(), k -> new ArrayList<>()).add(span);
        }
        childrenByParent.values().forEach(list -> list.sort(Comparator.comparingLong(SpanData::getStartEpochNanos)));
    }

    /**
     * Builds one {@link Call.Kind#REQUEST} per HTTP server span, in start order.
     *
     * @param spans all spans collected, in any order
     * @return request trees
     */
    public static List<Call> requests(List<SpanData> spans) {
        return new SpanTree(spans).roots(spans);
    }

    private List<Call> roots(List<SpanData> spans) {
        var byId = new HashMap<String, SpanData>();
        spans.forEach(s -> byId.put(s.getSpanId(), s));
        return spans.stream()
                .filter(s -> s.getKind() == SpanKind.SERVER && !hasServerAncestor(s, byId))
                .sorted(Comparator.comparingLong(SpanData::getStartEpochNanos))
                .map(this::request)
                .toList();
    }

    private static boolean hasServerAncestor(SpanData span, Map<String, SpanData> byId) {
        var parent = byId.get(span.getParentSpanId());
        while (parent != null) {
            if (parent.getKind() == SpanKind.SERVER) {
                return true;
            }
            parent = byId.get(parent.getParentSpanId());
        }
        return false;
    }

    private Call request(SpanData server) {
        var children = convertChildren(server);
        var label = "";
        for (int i = 0; i < children.size(); i++) {
            var child = children.get(i);
            if (child.kind() == Call.Kind.COMPONENT && child.label().split("\\.(?=[^.]*$)")[0].endsWith(CONTROLLER_SUFFIX)) {
                label = child.label().substring(child.label().lastIndexOf('.') + 1);
                var folded = new ArrayList<>(children.subList(0, i));
                folded.addAll(child.children());
                folded.addAll(children.subList(i + 1, children.size()));
                children = folded;
                break;
            }
        }
        var attributes = new LinkedHashMap<String, String>();
        var status = firstNonNull(attr(server, STATUS_CODE_KEY), attr(server, SpanNames.STATUS_KEY));
        if (status != null) {
            attributes.put("status", status);
        }
        var method = Objects.toString(attr(server, METHOD_KEY), "").toUpperCase(Locale.ROOT);
        var detail = (method + " " + Objects.toString(attr(server, SpanNames.ROUTE_KEY), "")).trim();
        return new Call(Call.Kind.REQUEST, label, detail, attributes, collapse(children));
    }

    private List<Call> convertChildren(SpanData parent) {
        var result = new ArrayList<Call>();
        for (SpanData child : childrenByParent.getOrDefault(parent.getSpanId(), List.of())) {
            result.addAll(convert(child));
        }
        return result;
    }

    private List<Call> convert(SpanData span) {
        var name = span.getName();
        if (isChat(span)) {
            return List.of(leaf(Call.Kind.GEMINI_CHAT, name, Map.of()));
        }
        if (SpanNames.EMBEDDING_SPAN.equals(name)) {
            return List.of(leaf(Call.Kind.GEMINI_EMBED, name, Map.of()));
        }
        if (SpanNames.COMPONENT_SPAN.equals(name)) {
            // Mockito subclasses carry a random suffix; it would make the diagram differ between runs
            var namespace = Objects.toString(attr(span, SpanNames.NAMESPACE_KEY), "").replaceAll("\\$MockitoMock\\$\\w+", "");
            var label = namespace + "." + attr(span, SpanNames.FUNCTION_KEY);
            return List.of(new Call(Call.Kind.COMPONENT, label, "", Map.of(), collapse(convertChildren(span))));
        }
        var sql = sqlStatements(span);
        if (!sql.isEmpty()) {
            var calls = new ArrayList<Call>();
            sql.forEach(s -> calls.add(leaf(Call.Kind.SQL, s, Map.of())));
            calls.addAll(convertChildren(span));
            return calls;
        }
        if (span.getKind() == SpanKind.CLIENT) {
            return List.of(piper(span));
        }
        return convertChildren(span);
    }

    private static Call piper(SpanData span) {
        var method = Objects.toString(attr(span, METHOD_KEY), "").toUpperCase(Locale.ROOT);
        var url = attr(span, HTTP_URL_KEY);
        var path = url == null ? Objects.toString(attr(span, SpanNames.ROUTE_KEY), "") : URI.create(url).getPath();
        var attributes = new LinkedHashMap<String, String>();
        var status = attr(span, SpanNames.STATUS_KEY);
        if (status != null) {
            attributes.put("status", status);
        }
        return leaf(Call.Kind.PIPER, (method + " " + path).trim(), attributes);
    }

    private static Call leaf(Call.Kind kind, String label, Map<String, String> attributes) {
        return new Call(kind, label, "", attributes, List.of());
    }

    private static boolean isChat(SpanData span) {
        return span.getName().startsWith(CHAT_CLIENT_PREFIX) || "chat_client".equals(attr(span, AI_KIND_KEY))
                || "chat".equals(attr(span, OPERATION_KEY));
    }

    private static List<String> sqlStatements(SpanData span) {
        var byIndex = new TreeMap<Integer, String>();
        span.getAttributes().forEach((key, value) -> {
            var k = key.getKey();
            if (k.startsWith(JDBC_PREFIX) && k.endsWith("]")) {
                byIndex.put(Integer.parseInt(k.substring(JDBC_PREFIX.length(), k.length() - 1)), String.valueOf(value));
            }
        });
        return List.copyOf(byIndex.values());
    }

    private static List<Call> collapse(List<Call> calls) {
        var result = new ArrayList<Call>();
        int run = 0;
        for (Call call : calls) {
            var last = result.isEmpty() ? null : result.getLast();
            if (call.kind() == Call.Kind.SQL && last != null && last.kind() == Call.Kind.SQL
                    && last.label().equals(call.label())) {
                run++;
                result.set(result.size() - 1, new Call(Call.Kind.SQL, last.label(), "", Map.of(COUNT, String.valueOf(run)), List.of()));
            } else {
                run = 1;
                result.add(call);
            }
        }
        return List.copyOf(result);
    }

    private static String attr(SpanData span, String key) {
        var value = span.getAttributes().get(AttributeKey.stringKey(key));
        if (value != null) {
            return value;
        }
        var raw = span.getAttributes().asMap().entrySet().stream()
                .filter(e -> e.getKey().getKey().equals(key)).findFirst();
        return raw.map(e -> String.valueOf(e.getValue())).orElse(null);
    }

    private static String firstNonNull(String a, String b) {
        return a != null ? a : b;
    }
}
