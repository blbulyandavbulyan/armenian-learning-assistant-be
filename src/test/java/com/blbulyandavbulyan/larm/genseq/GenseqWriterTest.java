package com.blbulyandavbulyan.larm.genseq;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.blbulyandavbulyan.larm.genseq.Call.Kind;
import com.blbulyandavbulyan.larm.genseq.GenseqWriter.Diagram;
import com.blbulyandavbulyan.larm.genseq.GenseqWriter.TestRef;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GenseqWriterTest {
    private static final Pattern HEADER = Pattern.compile(
            "^(?:title|==)\\s*\\[\\[src://(?<path>[^\\s:{\\]]+):(?<line>\\d+)(?:\\{(?<tip>[^}]*)\\})?\\s*+(?:Test:\\s*+)?(?<title>[^\\]]*+)\\]\\]");
    private static final String TEST_PATH = "src/test/java/com/blbulyandavbulyan/larm/api/dialogues/DialogueControllerIT.java";
    private static final TestRef REF = new TestRef(TEST_PATH, 42, "DialogueControllerIT", "saves dialogue", "traced from a real run");

    private static Call call(Kind kind, String label, String detail, Map<String, String> attributes, Call... children) {
        return new Call(kind, label, detail, attributes, List.of(children));
    }

    private static Call sql(String text) {
        return call(Kind.SQL, text, "", Map.of());
    }

    private static Call request(Call... children) {
        return call(Kind.REQUEST, "saveDialogue", "POST /dialogues", Map.of("status", "201"), children);
    }

    @Test
    void headerMatchesTheSkillsHandleRegex() {
        String title = GenseqWriter.render(REF, List.of(request())).puml().lines()
                .filter(l -> l.startsWith("title")).findFirst().orElseThrow();
        Matcher matcher = HEADER.matcher(title);
        assertThat(matcher.find()).isTrue();
        assertThat(matcher.group("path")).isEqualTo(TEST_PATH);
        assertThat(matcher.group("line")).isEqualTo("42");
        assertThat(matcher.group("title").strip()).isEqualTo("DialogueControllerIT: saves dialogue");
    }

    @Test
    void requestArrowCarriesHandlerAndRoute() {
        String puml = GenseqWriter.render(REF, List.of(request())).puml();
        assertThat(puml).contains("Test -> Backend: [[genseq://")
                .contains("{Click for the request as it was sent} saveDialogue\\nPOST /dialogues ⊕]]")
                .contains("Backend --> Test: 201");
    }

    @Test
    void componentLinksToItsSourceLine() throws IOException {
        String path = "src/main/java/com/blbulyandavbulyan/larm/core/DialogueOrchestrator.java";
        List<String> lines = Files.readAllLines(Path.of(path));
        int expected = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).contains("public SavedDialogueResource saveDialogue(")) {
                expected = i + 1;
                break;
            }
        }
        assertThat(expected).isPositive();
        Call component = call(Kind.COMPONENT, "DialogueOrchestrator.saveDialogue", "", Map.of(), sql("select 1"));
        String puml = GenseqWriter.render(REF, List.of(request(component))).puml();
        assertThat(puml).contains("[[src://" + path + ":" + expected
                + "{Click to open DialogueOrchestrator.saveDialogue} DialogueOrchestrator.saveDialogue ↗]]");
    }

    @Test
    void unknownComponentIsPlainText() {
        Call component = call(Kind.COMPONENT, "NoSuchClass.run", "", Map.of());
        String puml = GenseqWriter.render(REF, List.of(request(component))).puml();
        assertThat(puml).contains("Backend -> Backend: NoSuchClass.run\n");
    }

    @Test
    void onlyUsedLifelinesAreDeclared() {
        String none = GenseqWriter.render(REF, List.of(request())).puml();
        assertThat(none).contains("participant Test\nparticipant Backend\n")
                .doesNotContain("participant DB").doesNotContain("participant Piper").doesNotContain("participant Gemini");
        String all = GenseqWriter.render(REF, List.of(request(
                call(Kind.GEMINI_EMBED, "", "", Map.of()),
                call(Kind.PIPER, "POST /api/tts", "", Map.of()),
                sql("select 1")))).puml();
        assertThat(all).contains("participant Backend\nparticipant DB\nparticipant Piper\nparticipant Gemini\n")
                .contains("Backend -> Piper: POST /api/tts").contains("Backend -> Gemini: embed");
    }

    @Test
    void collapsedSqlShowsCount() {
        Call collapsed = call(Kind.SQL, "insert into t values (?)", "", Map.of("count", "3"));
        String puml = GenseqWriter.render(REF, List.of(request(collapsed))).puml();
        assertThat(puml).contains("insert into t values (?) ×3 ⊕]]");
    }

    @Test
    void longSqlIsTruncatedInDiagramButFullInJson() {
        String longSql = "select " + "x, ".repeat(100) + "y from t";
        Diagram diagram = GenseqWriter.render(REF, List.of(request(sql(longSql))));
        assertThat(diagram.puml()).contains(longSql.substring(0, 120) + " … ⊕]]");
        assertThat(diagram.json()).contains(longSql);
    }

    @Test
    void linkSyntaxCannotBeBrokenByLabels() {
        Call evil = call(Kind.SQL, "select 'a]]b'\nfrom t", "", Map.of());
        String puml = GenseqWriter.render(REF, List.of(request(evil))).puml();
        String line = puml.lines().filter(l -> l.startsWith("Backend -> DB")).findFirst().orElseThrow();
        assertThat(line).doesNotContain("\n").endsWith(" ⊕]]");
        assertThat(line.indexOf("]]")).isEqualTo(line.length() - 2);
    }

    @Test
    void routeTemplateBracesAreEncoded() {
        Call req = new Call(Kind.REQUEST, "getDialogue", "GET /dialogues/{id}", Map.of("status", "200"), List.of());
        String puml = GenseqWriter.render(REF, List.of(req)).puml();
        assertThat(puml).contains("GET /dialogues/<U+007B>id<U+007D> ⊕]]").doesNotContain("{id}");
    }

    @Test
    void renderingTwiceIsByteIdentical() {
        List<Call> calls = List.of(request(sql("select 1"), call(Kind.PIPER, "POST /x", "", Map.of())));
        Diagram first = GenseqWriter.render(REF, calls);
        Diagram second = GenseqWriter.render(REF, calls);
        assertThat(second).isEqualTo(first);
        assertThat(first.puml()).endsWith("@enduml\n").doesNotContain("\r");
        assertThat(first.json()).endsWith("\n").doesNotContain("\r");
    }

    @Test
    void idsDependOnContentNotOnRunOrder() {
        Map<String, String> one = new java.util.LinkedHashMap<>();
        one.put("status", "201");
        one.put("extra", "a");
        Map<String, String> two = new java.util.LinkedHashMap<>();
        two.put("extra", "a");
        two.put("status", "201");
        Call a = call(Kind.REQUEST, "h", "GET /x", one, sql("select 1"));
        Call b = call(Kind.REQUEST, "h", "GET /x", two, sql("select 1"));
        assertThat(GenseqWriter.render(REF, List.of(a))).isEqualTo(GenseqWriter.render(REF, List.of(b)));
        Call other = call(Kind.REQUEST, "h", "GET /x", one, sql("select 2"));
        assertThat(GenseqWriter.render(REF, List.of(other)).json())
                .isNotEqualTo(GenseqWriter.render(REF, List.of(a)).json());
    }

    @Test
    void jsonKeysAreSorted() {
        String json = GenseqWriter.render(REF, List.of(request(sql("select 1"), sql("select 2")))).json();
        assertThat(json).startsWith("{\n  \"details\": ").contains("\"version\": 3");
        Matcher ids = Pattern.compile("^    \"([0-9a-z]{7})\": \\{$", Pattern.MULTILINE).matcher(json);
        List<String> found = new java.util.ArrayList<>();
        while (ids.find()) {
            found.add(ids.group(1));
        }
        assertThat(found).hasSize(3).isSorted();
        assertThat(json.indexOf("\"steps\"")).isLessThan(json.indexOf("\"title\""));
    }

    @Test
    void slugOfCamelCaseAndUnderscores() {
        assertThat(GenseqWriter.slug("saveDialogue_withEmptyTranslations")).isEqualTo("save-dialogue-with-empty-translations");
        assertThat(GenseqWriter.slug("serves")).isEqualTo("serves");
    }

    @Test
    void truncationDoesNotSplitASurrogatePair() {
        String sql = "x".repeat(119) + "\uD83D\uDE00" + "y".repeat(20);
        String puml = GenseqWriter.render(REF, List.of(request(sql(sql)))).puml();
        assertThat(puml).doesNotContain("\uD83D").contains(" …");
        assertThat(java.nio.charset.StandardCharsets.UTF_8.newEncoder().canEncode(puml)).isTrue();
    }
}
