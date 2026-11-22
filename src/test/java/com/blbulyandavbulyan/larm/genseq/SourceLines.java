package com.blbulyandavbulyan.larm.genseq;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Finds production source locations; paths are resolved from the working directory (the repository root).
 */
final class SourceLines {
    private static final Path MAIN_SOURCES = Path.of("src/main/java");
    private static final Pattern NOT_A_DECLARATION = Pattern.compile("^\\s*(return|new|throw|else|\\.|//|\\*|/\\*)\\b?.*");

    private SourceLines() {
    }

    /**
     * Repo-relative path (with forward slashes) of {@code src/main/java/**}/{@code <simpleClassName>.java}.
     */
    static Optional<String> sourcePath(String simpleClassName) {
        if (!Files.isDirectory(MAIN_SOURCES)) {
            return Optional.empty();
        }
        try (Stream<Path> files = Files.walk(MAIN_SOURCES)) {
            return files.filter(p -> p.getFileName().toString().equals(simpleClassName + ".java"))
                    .map(p -> p.toString().replace('\\', '/'))
                    .sorted()
                    .findFirst();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 1-based line of the first declaration of {@code method} in the class source, if found.
     */
    static OptionalInt methodLine(String simpleClassName, String method) {
        Optional<String> path = sourcePath(simpleClassName);
        if (path.isEmpty()) {
            return OptionalInt.empty();
        }
        Pattern call = Pattern.compile("\\b" + Pattern.quote(method) + "\\s*\\(");
        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of(path.get()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (call.matcher(line).find() && isDeclaration(line, method)) {
                return OptionalInt.of(i + 1);
            }
        }
        return OptionalInt.empty();
    }

    private static boolean isDeclaration(String line, String method) {
        if (NOT_A_DECLARATION.matcher(line).matches()) {
            return false;
        }
        String before = line.substring(0, line.indexOf(method)).trim();
        // a declaration has a return type (or modifiers) before the name and no call-ish punctuation
        return !before.isEmpty() && !before.contains("=") && !before.contains("(") && !before.endsWith(".");
    }
}
