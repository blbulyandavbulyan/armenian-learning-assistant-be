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

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Writes a sequence diagram from the real spans of each selected integration test that passed.
 */
@Slf4j
public class SequenceDiagramExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback, AfterEachCallback {
    private static final String REQUESTS_KEY = "requests";
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

    /**
     * Pitest runs the tests against mutated code in minion JVMs; a surviving mutant must not overwrite the diagrams.
     */
    static boolean isMutationRun(String javaCommand) {
        return javaCommand != null && javaCommand.startsWith("org.pitest.");
    }

    static boolean shouldWrite(boolean selected, boolean testFailed, int requestCount) {
        return selected && !testFailed && requestCount > 0;
    }

    @Override
    public void beforeTestExecution(@NonNull ExtensionContext context) {
        if (selected(context) != null) {
            TracingTestConfiguration.reset(applicationContext(context));
        }
    }

    /**
     * Captures the spans right after the test body, so calls made by fixtures in {@code @BeforeEach} or
     * {@code @AfterEach} methods never reach the diagram.
     */
    @Override
    public void afterTestExecution(@NonNull ExtensionContext context) {
        if (selected(context) == null || context.getExecutionException().isPresent()) {
            return;
        }
        try {
            List<Call> requests = SpanTree.requests(TracingTestConfiguration.finishedSpans(applicationContext(context)));
            store(context).put(REQUESTS_KEY, requests);
        } catch (RuntimeException e) {
            log.warn("Not writing a sequence diagram for {}: {}", context.getDisplayName(), e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterEach(@NonNull ExtensionContext context) {
        List<Call> requests = (List<Call>) store(context).remove(REQUESTS_KEY);
        Selection selection = selected(context);
        boolean failed = context.getExecutionException().isPresent();
        if (selection == null || requests == null || !shouldWrite(true, failed, requests.size())) {
            return;
        }
        Class<?> testClass = context.getRequiredTestClass();
        Method testMethod = context.getRequiredTestMethod();
        String method = testMethod.getName();
        Class<?> declaring = topLevel(testMethod.getDeclaringClass());
        String path = TEST_SOURCES + declaring.getName().replace('.', '/') + ".java";
        int line = declarationLine(path, method);
        GenseqWriter.TestRef ref = new GenseqWriter.TestRef(path, line, testClass.getSimpleName(),
                scenario(method), selection.annotated() ? TAGGED_FOOTER : SELECTED_FOOTER);
        GenseqWriter.Diagram diagram = GenseqWriter.render(ref, requests);
        String base = testClass.getSimpleName() + ".java." + GenseqWriter.slug(method) + ".genseq";
        write(Path.of(OUTPUT_DIR, base + ".puml"), diagram.puml());
        write(Path.of(OUTPUT_DIR, base + ".json"), diagram.json());
    }

    private static ExtensionContext.Store store(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(SequenceDiagramExtension.class, context.getUniqueId()));
    }

    private static Class<?> topLevel(Class<?> type) {
        Class<?> current = type;
        while (current.getEnclosingClass() != null) {
            current = current.getEnclosingClass();
        }
        return current;
    }

    private static String scenario(String method) {
        return GenseqWriter.slug(method).replace('-', ' ');
    }

    private static ApplicationContext applicationContext(ExtensionContext context) {
        return SpringExtension.getApplicationContext(context);
    }

    private static Selection selected(ExtensionContext context) {
        if (isMutationRun(System.getProperty("sun.java.command"))) {
            return null;
        }
        Method method = context.getRequiredTestMethod();
        Class<?> testClass = context.getRequiredTestClass();
        boolean annotated = method.isAnnotationPresent(GenerateSequence.class)
                || testClass.isAnnotationPresent(GenerateSequence.class);
        String select = System.getProperty(SELECT_KEY, System.getenv(SELECT_KEY));
        return isSelected(testClass, method.getName(), annotated, select) ? new Selection(annotated) : null;
    }

    /** Line of the test method, or 1 (with a warning) when the declaration cannot be found. */
    private static int declarationLine(String path, String method) {
        try {
            List<String> lines = Files.readAllLines(Path.of(path));
            for (int i = 0; i < lines.size(); i++) {
                if (declarationPattern(method).matcher(lines.get(i)).find()) {
                    return i + 1;
                }
            }
        } catch (IOException e) {
            log.warn("Cannot read {}: {}", path, e.getMessage());
            return 1;
        }
        log.warn("No declaration of void {}( found in {}", method, path);
        return 1;
    }

    static Pattern declarationPattern(String method) {
        return Pattern.compile("^\\s*(?:(?:@\\w+(?:\\([^)]*\\))?|public|protected|private|static|final)\\s+)*void\\s+"
                + Pattern.quote(method) + "\\s*\\(");
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
