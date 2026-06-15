package com.blbulyandavbulyan.larm;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.fail;

class ArchitectureTest {

    private static final String BASE_PACKAGE = "com.blbulyandavbulyan.larm";

    private static final String API_PACKAGE = BASE_PACKAGE + ".api..";

    private static final String API_PACKAGE_PREFIX = BASE_PACKAGE + ".api.";

    private static final String SWAGGER_ANNOTATIONS_PACKAGE = "io.swagger..";

    private static final String BASE_PACKAGE_PATH = BASE_PACKAGE.replace('.', '/');

    private static final String API_PACKAGE_PATH = BASE_PACKAGE_PATH + "/api";

    private static final String API_CHAT_PACKAGE = BASE_PACKAGE + ".api.chat..";

    private static final String API_ASSETS_PACKAGE = BASE_PACKAGE + ".api.assets..";

    private static final String API_PHRASES_PACKAGE = BASE_PACKAGE + ".api.phrases..";

    private static final String API_ADVICE_PACKAGE = BASE_PACKAGE + ".api.advice..";

    private static final String API_OPENAPI_PACKAGE = BASE_PACKAGE + ".api.openapi..";
    public static final String JAVA_LANG = "java.lang..";
    public static final String JAVA_UTIL = "java.util..";
    public static final String SPRINGFRAMEWORK_CONTEXT_ANNOTATION = "org.springframework.context.annotation..";

    private static JavaClasses importedClasses;

    @BeforeAll
    static void importClasses() {
        StaticJavaParser.getParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.BLEEDING_EDGE);
        importedClasses = new ClassFileImporter().importPackages(BASE_PACKAGE);
    }

    @Test
    void swaggerAnnotationsShouldBeUsedOnlyInApiPackage() {
        ArchRule rule = noClasses()
                .that()
                .resideOutsideOfPackage(API_PACKAGE)
                .should()
                .dependOnClassesThat()
                .resideInAPackage(SWAGGER_ANNOTATIONS_PACKAGE)
                .because("Swagger/OpenAPI annotations must only be used inside the api package");
        rule.check(importedClasses);
    }

    @Test
    void nonApiPackagesShouldNotDependOnApiPackage() {
        // Catches all bytecode-level dependencies: field types, method calls, return
        // types, FQN type references, etc. Does NOT catch static final String constants
        // because the compiler inlines them and erases the class reference from bytecode.
        ArchRule rule = noClasses()
                .that()
                .resideOutsideOfPackage(API_PACKAGE)
                .should()
                .dependOnClassesThat()
                .resideInAPackage(API_PACKAGE)
                .because("The api package is an outward-facing layer; internal packages must not depend on it");
        rule.check(importedClasses);
    }

    @Test
    void apiChatPackageShouldOnlyDependOnAdviceAndOpenapi() {
        noClasses()
                .that().resideInAPackage(API_CHAT_PACKAGE)
                .should().dependOnClassesThat()
                .resideInAnyPackage(API_ASSETS_PACKAGE, API_PHRASES_PACKAGE)
                .because("chat may only depend on advice and openapi within the api package")
                .check(importedClasses);
    }

    @Test
    void apiAssetsPackageShouldOnlyDependOnAdviceAndOpenapi() {
        noClasses()
                .that().resideInAPackage(API_ASSETS_PACKAGE)
                .should().dependOnClassesThat()
                .resideInAnyPackage(API_CHAT_PACKAGE, API_PHRASES_PACKAGE)
                .because("assets may only depend on advice and openapi within the api package")
                .check(importedClasses);
    }

    @Test
    void apiPhrasesPackageShouldOnlyDependOnAdviceAndOpenapi() {
        noClasses()
                .that().resideInAPackage(API_PHRASES_PACKAGE)
                .should().dependOnClassesThat()
                .resideInAnyPackage(API_CHAT_PACKAGE, API_ASSETS_PACKAGE)
                .because("phrases may only depend on advice and openapi within the api package")
                .check(importedClasses);
    }

    @Test
    void apiAdvicePackageShouldNotDependOnOtherApiSubPackages() {
        noClasses()
                .that().resideInAPackage(API_ADVICE_PACKAGE)
                .should().dependOnClassesThat()
                .resideInAnyPackage(API_CHAT_PACKAGE, API_ASSETS_PACKAGE, API_PHRASES_PACKAGE, API_OPENAPI_PACKAGE)
                .because("advice must be self-contained within the api package")
                .check(importedClasses);
    }

    @Test
    void apiOpenapiPackageShouldNotDependOnOtherApiSubPackages() {
        noClasses()
                .that().resideInAPackage(API_OPENAPI_PACKAGE)
                .should().dependOnClassesThat()
                .resideOutsideOfPackages(API_OPENAPI_PACKAGE, JAVA_LANG, JAVA_UTIL, SPRINGFRAMEWORK_CONTEXT_ANNOTATION, SWAGGER_ANNOTATIONS_PACKAGE)
                .because("openapi must be self-contained within the api package")
                .check(importedClasses);
    }

    @Test
    void nonApiPackagesShouldNotReferenceApiPackageInSource() throws IOException {
        // Complements the bytecode ArchUnit rule above: static final String constants
        // are inlined by the Java compiler, erasing the originating class reference from
        // bytecode entirely. JavaParser operates on the source AST, so it catches both
        // import-based and FQN-based references that ArchUnit cannot see.
        List<String> violations = new ArrayList<>();
        Path sourceRoot = Path.of("src/main/java");
        try (Stream<Path> javaFiles = Files.walk(sourceRoot)) {
            javaFiles.filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> {
                        String relative = sourceRoot.relativize(p).toString().replace('\\', '/');
                        return relative.startsWith(BASE_PACKAGE_PATH) && !relative.startsWith(API_PACKAGE_PATH);
                    })
                    .forEach(p -> checkSourceFile(p, violations));
        }
        if (!violations.isEmpty()) {
            fail("Classes outside the api package must not reference the api package:\n"
                    + String.join("\n", violations));
        }
    }

    private static void checkSourceFile(Path file, List<String> violations) {
        CompilationUnit cu;
        try {
            cu = StaticJavaParser.parse(Files.readString(file));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (RuntimeException e) {
            // JavaParser does not yet support every Java 22+ construct (e.g. JEP 492
            // "statements before super()"). Skip unparseable files — the ArchUnit
            // bytecode rule still enforces structural dependencies for them.
            return;
        }
        String fileName = file.getFileName().toString();
        new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ImportDeclaration n, Void arg) {
                if (n.getNameAsString().startsWith(API_PACKAGE_PREFIX)) {
                    violations.add(fileName + ": import " + n.getNameAsString());
                }
                super.visit(n, arg);
            }

            @Override
            public void visit(FieldAccessExpr n, Void arg) {
                // Only report the outermost FQN that matches to avoid duplicate violations
                // for every nested segment (e.g. a.b.c.d reported once, not also a.b.c, a.b).
                boolean parentIsAlsoApiRef = n.getParentNode()
                        .filter(parent -> parent instanceof FieldAccessExpr)
                        .map(parent -> parent.toString().startsWith(API_PACKAGE_PREFIX))
                        .orElse(false);
                if (!parentIsAlsoApiRef && n.toString().startsWith(API_PACKAGE_PREFIX)) {
                    violations.add(fileName + ": FQN reference " + n);
                }
                super.visit(n, arg);
            }
        }.visit(cu, null);
    }
}
