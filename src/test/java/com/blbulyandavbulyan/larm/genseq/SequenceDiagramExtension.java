package com.blbulyandavbulyan.larm.genseq;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Writes a sequence diagram from the real spans of each selected integration test that passed.
 */
public class SequenceDiagramExtension implements BeforeEachCallback, AfterEachCallback {
    static final String OUTPUT_DIR = "docs/generated/sequence";
    private static final String SELECT_KEY = "GENSEQ_SELECT";
    private static final String TEST_SOURCES = "src/test/java/";
    private static final String TAGGED_FOOTER = "traced because it is tagged @GenerateSequence";
    private static final String SELECTED_FOOTER =
            "traced because human-review selected it — generated from real traces of integration test runs, do not edit ❗";

    static boolean isSelected(Class<?> testClass, String method, boolean annotated, String genseqSelect) {
        if (annotated) {
            return true;
        }
        if (genseqSelect == null || genseqSelect.isBlank()) {
            return false;
        }
        return Arrays.stream(genseqSelect.split(","))
                .map(String::trim)
                .anyMatch(entry -> entry.equals(testClass.getSimpleName() + "#" + method)
                        || entry.equals(testClass.getName() + "#" + method));
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        if (selected(context) != null) {
            TracingTestConfiguration.reset(applicationContext(context));
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        Selection selection = selected(context);
        if (selection == null || context.getExecutionException().isPresent()) {
            return;
        }
        List<Call> requests = SpanTree.requests(TracingTestConfiguration.finishedSpans(applicationContext(context)));
        if (requests.isEmpty()) {
            return;
        }
        Class<?> testClass = context.getRequiredTestClass();
        String method = context.getRequiredTestMethod().getName();
        String path = TEST_SOURCES + testClass.getName().replace('.', '/') + ".java";
        int line = declarationLine(testClass, path, method);
        GenseqWriter.TestRef ref = new GenseqWriter.TestRef(path, line, testClass.getSimpleName(),
                scenario(method), selection.annotated() ? TAGGED_FOOTER : SELECTED_FOOTER);
        GenseqWriter.Diagram diagram = GenseqWriter.render(ref, requests);
        String base = testClass.getSimpleName() + ".java." + GenseqWriter.slug(method) + ".genseq";
        write(Path.of(OUTPUT_DIR, base + ".puml"), diagram.puml());
        write(Path.of(OUTPUT_DIR, base + ".json"), diagram.json());
    }

    private static String scenario(String method) {
        return GenseqWriter.slug(method).replace('-', ' ');
    }

    private static ApplicationContext applicationContext(ExtensionContext context) {
        return SpringExtension.getApplicationContext(context);
    }

    private static Selection selected(ExtensionContext context) {
        Method method = context.getRequiredTestMethod();
        Class<?> testClass = context.getRequiredTestClass();
        boolean annotated = method.isAnnotationPresent(GenerateSequence.class)
                || testClass.isAnnotationPresent(GenerateSequence.class);
        String select = System.getProperty(SELECT_KEY, System.getenv(SELECT_KEY));
        return isSelected(testClass, method.getName(), annotated, select) ? new Selection(annotated) : null;
    }

    private static int declarationLine(Class<?> testClass, String path, String method) {
        Pattern declaration = Pattern.compile("^\\s*(?:(?:public|protected|private|static|final)\\s+)*void\\s+"
                + Pattern.quote(method) + "\\s*\\(");
        try {
            List<String> lines = Files.readAllLines(Path.of(path));
            for (int i = 0; i < lines.size(); i++) {
                if (declaration.matcher(lines.get(i)).find()) {
                    return i + 1;
                }
            }
            throw new IllegalStateException("No declaration of void " + method + "( found in " + testClass.getName());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void write(Path file, String content) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, content.replace("\r\n", "\n"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private record Selection(boolean annotated) {
    }
}
