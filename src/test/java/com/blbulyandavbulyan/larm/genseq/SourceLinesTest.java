package com.blbulyandavbulyan.larm.genseq;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.OptionalInt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SourceLinesTest {
    @Test
    void callSitesAreNotDeclarationsAndAnnotatedDeclarationIs() throws IOException {
        Path file = Path.of("src/main/java/com/blbulyandavbulyan/larm/SourceLinesFixture.java");
        Files.createDirectories(file.getParent());
        Files.writeString(file, String.join("\n",
                "package x;",
                "class SourceLinesFixture {",
                "    void a(int k) {",
                "        switch (k) {",
                "            case 1 -> get(k);",
                "            default -> { yield get(k); }",
                "        }",
                "        String widget = get(\"key\");",
                "    }",
                "    @Deprecated(since = \"1\") public String get(String k) { return k; }",
                "}", ""));
        try {
            assertThat(SourceLines.methodLine("SourceLinesFixture", "get")).isEqualTo(OptionalInt.of(10));
        } finally {
            Files.delete(file);
        }
    }
}
