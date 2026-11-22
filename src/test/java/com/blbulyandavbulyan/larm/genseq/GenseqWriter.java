package com.blbulyandavbulyan.larm.genseq;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeMap;

import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.core.util.Separators;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Renders call trees as genseq PlantUML plus the JSON holding the details behind each link.
 */
final class GenseqWriter {
    private static final int SQL_LIMIT = 120;
    private static final int ID_LENGTH = 7;
    private static final JsonMapper MAPPER = JsonMapper.builder()
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .build();

    private GenseqWriter() {
    }

    /**
     * Rendered diagram files.
     *
     * @param puml PlantUML source
     * @param json details JSON
     */
    record Diagram(String puml, String json) {
    }

    /**
     * The test a diagram belongs to.
     *
     * @param testClassPath repo-relative path of the test source
     * @param line          line of the test method
     * @param className     simple name of the test class
     * @param scenario      human readable scenario
     * @param footer        footer text
     */
    record TestRef(String testClassPath, int line, String className, String scenario, String footer) {
    }

    static Diagram render(TestRef test, List<Call> requests) {
        Rendering rendering = new Rendering();
        for (Call request : requests) {
            rendering.emit(request);
        }
        StringBuilder puml = new StringBuilder();
        puml.append("@startuml\n")
                .append("' ⚠️  GENERATED FILE — DO NOT EDIT. Every edit is lost on the next run.\n")
                .append("hide footbox\n")
                .append("skinparam hyperlinkUnderline false\n")
                .append("skinparam hyperlinkColor #1A4FA0\n")
                .append("skinparam sequenceGroupFontColor #888888\n")
                .append("skinparam sequenceGroupFontStyle plain\n")
                .append("title [[src://").append(test.testClassPath()).append(':').append(test.line())
                .append("{Click to open the test} Test: ").append(safe(test.className())).append(": ")
                .append(safe(test.scenario())).append("]]\n")
                .append("footer ").append(oneLine(test.footer())).append('\n')
                .append("participant Test\nparticipant Backend\n");
        for (String lifeline : List.of("DB", "Piper", "Gemini")) {
            if (rendering.lifelines.contains(lifeline)) {
                puml.append("participant ").append(lifeline).append('\n');
            }
        }
        puml.append(rendering.body).append("@enduml\n");
        return new Diagram(puml.toString(), json(rendering.details));
    }

    static String slug(String methodName) {
        return methodName.replaceAll("([a-z0-9])([A-Z])", "$1-$2")
                .replaceAll("[^A-Za-z0-9]+", "-")
                .replaceAll("^-|-$", "")
                .toLowerCase(java.util.Locale.ROOT);
    }

    private static String json(Map<String, Object> details) {
        Map<String, Object> root = new TreeMap<>();
        root.put("version", 3);
        root.put("details", details);
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter(
                Separators.createDefaultInstance().withObjectNameValueSpacing(Separators.Spacing.AFTER))
                .withObjectIndenter(new DefaultIndenter("  ", "\n"))
                .withArrayIndenter(new DefaultIndenter("  ", "\n"));
        return MAPPER.writer().with(printer).writeValueAsString(root).replace("\r", "") + "\n";
    }

    private static String id(Call call, int index) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(
                    (call.kind() + "|" + call.label() + "|" + call.detail() + "|" + index).getBytes(StandardCharsets.UTF_8));
            String base36 = new BigInteger(1, hash).toString(36);
            return base36.substring(0, Math.min(ID_LENGTH, base36.length()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String oneLine(String text) {
        return text.replaceAll("\\s+", " ").trim();
    }

    /** Text safe inside {@code [[...]]}: one line, no premature link end, no tooltip end. */
    private static String safe(String text) {
        return oneLine(text).replace("]]", "] ]").replace("}", ")");
    }

    private static String truncate(String sql) {
        String line = oneLine(sql);
        return line.length() > SQL_LIMIT ? line.substring(0, SQL_LIMIT) + " …" : line;
    }

    private static final class Rendering {
        private final StringBuilder body = new StringBuilder();
        private final Set<String> lifelines = new java.util.HashSet<>();
        private final Map<String, Object> details = new TreeMap<>();
        private int index;

        void emit(Call call) {
            int myIndex = index++;
            String id = id(call, myIndex);
            switch (call.kind()) {
                case REQUEST -> request(call, id);
                case COMPONENT -> component(call);
                case SQL -> sql(call, id);
                case PIPER -> {
                    lifelines.add("Piper");
                    body.append("Backend -> Piper: ").append(oneLine(call.label())).append('\n');
                }
                case GEMINI_CHAT -> {
                    lifelines.add("Gemini");
                    body.append("Backend -> Gemini: chat\n");
                }
                case GEMINI_EMBED -> {
                    lifelines.add("Gemini");
                    body.append("Backend -> Gemini: embed\n");
                }
                default -> throw new IllegalStateException(String.valueOf(call.kind()));
            }
        }

        private void request(Call call, String id) {
            String text = call.label().isEmpty()
                    ? safe(call.detail()) : safe(call.label()) + "\\n" + safe(call.detail());
            body.append("Test -> Backend: [[genseq://").append(id)
                    .append("{Click for the request as it was sent} ").append(text).append(" ⊕]]\n")
                    .append("activate Backend\n");
            detail(id, "Test → Backend: " + oneLine(call.detail()), "request", call.detail());
            children(call);
            body.append("Backend --> Test: ").append(oneLine(call.attributes().getOrDefault("status", ""))).append('\n')
                    .append("deactivate Backend\n");
        }

        private void component(Call call) {
            String label = safe(call.label());
            int dot = call.label().indexOf('.');
            String link = null;
            if (dot > 0) {
                String className = call.label().substring(0, dot);
                String method = call.label().substring(dot + 1);
                OptionalInt line = SourceLines.methodLine(className, method);
                if (line.isPresent()) {
                    link = "[[src://" + SourceLines.sourcePath(className).orElseThrow() + ':' + line.getAsInt()
                            + "{Click to open " + label + "} " + label + " ↗]]";
                }
            }
            body.append("Backend -> Backend: ").append(link == null ? oneLine(call.label()) : link).append('\n');
            if (!call.children().isEmpty()) {
                body.append("activate Backend\n");
                children(call);
                body.append("deactivate Backend\n");
            }
        }

        private void sql(Call call, String id) {
            lifelines.add("DB");
            String count = call.attributes().get("count");
            String shown = safe(truncate(call.label())) + (count == null ? "" : " ×" + count);
            body.append("Backend -> DB: [[genseq://").append(id)
                    .append("{Click for the statement behind this call} ").append(shown).append(" ⊕]]\n");
            detail(id, truncate(call.label()), "", call.label());
        }

        private void children(Call call) {
            for (Call child : call.children()) {
                emit(child);
            }
        }

        private void detail(String id, String title, String label, String text) {
            Map<String, Object> step = new LinkedHashMap<>();
            step.put("label", label);
            step.put("text", text);
            Map<String, Object> entry = new TreeMap<>();
            entry.put("title", title);
            entry.put("steps", new ArrayList<>(List.of(step)));
            details.put(id, entry);
        }
    }
}
