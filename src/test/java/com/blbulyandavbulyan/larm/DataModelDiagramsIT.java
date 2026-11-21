package com.blbulyandavbulyan.larm;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regenerates the committed data-model diagrams under {@code docs/generated/}, which the human-review skill diffs
 * against {@code main} on its Data tab.
 *
 * <ul>
 *     <li>{@code DomainModel.puml}: the JPA entities and their associations, read from Hibernate's metamodel.</li>
 *     <li>{@code DB.puml}: the real PostgreSQL schema after all Flyway migrations ran, read from the catalog.</li>
 * </ul>
 *
 * <p>Output is sorted, so a file only changes when the model does. CI fails when the committed copies are stale.
 */
class DataModelDiagramsIT extends BaseIT {

    private static final Path OUTPUT_DIR = Path.of("docs", "generated");

    private static final String TABLES_AND_COLUMNS =
            """
            SELECT c.relname AS table_name,
                   a.attname AS column_name,
                   format_type(a.atttypid, a.atttypmod) AS column_type,
                   a.attnotnull AS not_null,
                   EXISTS (SELECT 1 FROM pg_index i
                           WHERE i.indrelid = c.oid AND i.indisprimary AND a.attnum = ANY (i.indkey)) AS primary_key,
                   EXISTS (SELECT 1 FROM pg_constraint fk
                           WHERE fk.conrelid = c.oid AND fk.contype = 'f' AND a.attnum = ANY (fk.conkey)) AS foreign_key
            FROM pg_class c
                     JOIN pg_namespace n ON n.oid = c.relnamespace
                     JOIN pg_attribute a ON a.attrelid = c.oid
            WHERE n.nspname = 'public'
              AND c.relkind = 'r'
              AND a.attnum > 0
              AND NOT a.attisdropped
              AND c.relname <> 'flyway_schema_history'
            ORDER BY c.relname, a.attnum
            """;

    private static final String FOREIGN_KEYS =
            """
            SELECT src.relname AS table_name,
                   src_col.attname AS column_name,
                   dst.relname AS referenced_table,
                   src_col.attnotnull AS not_null,
                   fk.confdeltype = 'c' AS cascade_delete
            FROM pg_constraint fk
                     JOIN pg_class src ON src.oid = fk.conrelid
                     JOIN pg_class dst ON dst.oid = fk.confrelid
                     JOIN pg_namespace n ON n.oid = src.relnamespace
                     JOIN pg_attribute src_col ON src_col.attrelid = src.oid AND src_col.attnum = fk.conkey[1]
            WHERE fk.contype = 'f'
              AND n.nspname = 'public'
            ORDER BY src.relname, src_col.attname
            """;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void writeDomainModelDiagram() throws IOException {
        List<EntityType<?>> entities = entityManagerFactory.getMetamodel().getEntities().stream()
                .sorted(Comparator.comparing(EntityType::getName))
                .toList();
        assertThat(entities).isNotEmpty();

        StringBuilder puml = header("Domain Model", "Generated from JPA entities by DataModelDiagramsIT");
        List<String> relationships = new ArrayList<>();
        for (EntityType<?> entity : entities) {
            puml.append("class ").append(entity.getName()).append(" {\n");
            for (Attribute<?, ?> attribute : sortedIdFirst(entity)) {
                if (attribute.isAssociation()) {
                    relationship(entity, attribute).ifPresent(relationships::add);
                } else {
                    puml.append("  ").append(attribute.getName()).append(" : ")
                            .append(typeName(attribute)).append('\n');
                }
            }
            puml.append("}\n");
        }
        puml.append('\n');
        relationships.stream().sorted().forEach(line -> puml.append(line).append('\n'));
        write("DomainModel.puml", puml);
    }

    @Test
    void writeDatabaseDiagram() throws IOException {
        Map<String, List<String>> tables = new LinkedHashMap<>();
        jdbcTemplate.query(TABLES_AND_COLUMNS, rs -> {
            StringBuilder column = new StringBuilder(rs.getString("column_name"))
                    .append(" : ").append(rs.getString("column_type"));
            if (rs.getBoolean("not_null")) {
                column.append(" NOT NULL");
            }
            if (rs.getBoolean("primary_key")) {
                column.append(" <<PK>>");
            }
            if (rs.getBoolean("foreign_key")) {
                column.append(" <<FK>>");
            }
            tables.computeIfAbsent(rs.getString("table_name"), name -> new ArrayList<>()).add(column.toString());
        });
        assertThat(tables).isNotEmpty();

        StringBuilder puml = header("DB", "Generated from the PostgreSQL schema after Flyway migrations by DataModelDiagramsIT");
        tables.forEach((table, columns) -> {
            puml.append("entity ").append(table).append(" {\n");
            columns.forEach(column -> puml.append("  ").append(column).append('\n'));
            puml.append("}\n");
        });
        puml.append('\n');
        jdbcTemplate.query(FOREIGN_KEYS, rs -> {
            // many rows of the referencing table point at exactly one (or, when nullable, at most one) row
            String target = rs.getBoolean("not_null") ? "||" : "o|";
            String label = rs.getString("column_name") + (rs.getBoolean("cascade_delete") ? " (on delete cascade)" : "");
            puml.append(rs.getString("table_name")).append(" }o--").append(target).append(' ')
                    .append(rs.getString("referenced_table")).append(" : ").append(label).append('\n');
        });
        write("DB.puml", puml);
    }

    private static StringBuilder header(String title, String caption) {
        return new StringBuilder(
                """
                @startuml
                !pragma layout smetana

                title %s
                caption %s

                hide empty members
                skinparam classAttributeIconSize 0

                """.formatted(title, caption));
    }

    private static List<Attribute<?, ?>> sortedIdFirst(EntityType<?> entity) {
        return entity.getAttributes().stream()
                .<Attribute<?, ?>>map(attribute -> attribute)
                .sorted(Comparator.<Attribute<?, ?>, Boolean>comparing(DataModelDiagramsIT::isNotId)
                        .thenComparing(Attribute::getName))
                .toList();
    }

    private static boolean isNotId(Attribute<?, ?> attribute) {
        return !(attribute instanceof SingularAttribute<?, ?> singular && singular.isId());
    }

    private static String typeName(Attribute<?, ?> attribute) {
        if (attribute instanceof PluralAttribute<?, ?, ?> plural) {
            return plural.getCollectionType().name().charAt(0)
                    + plural.getCollectionType().name().substring(1).toLowerCase()
                    + "<" + plural.getElementType().getJavaType().getSimpleName() + ">";
        }
        return attribute.getJavaType().getSimpleName();
    }

    /**
     * One line per association, drawn from its owning side only, so a bidirectional pair is not drawn twice.
     */
    private static Optional<String> relationship(EntityType<?> entity, Attribute<?, ?> attribute) {
        if (isInverseSide(attribute)) {
            return Optional.empty();
        }
        String owner = entity.getName();
        String label = " : " + attribute.getName();
        return Optional.of(switch (attribute.getPersistentAttributeType()) {
            case MANY_TO_ONE -> {
                String one = ((SingularAttribute<?, ?>) attribute).isOptional() ? "\"0..1\"" : "\"1\"";
                yield targetName(attribute) + " " + one + " -- \"0..*\" " + owner + label;
            }
            case ONE_TO_ONE -> {
                String one = ((SingularAttribute<?, ?>) attribute).isOptional() ? "\"0..1\"" : "\"1\"";
                yield owner + " \"0..1\" -- " + one + " " + targetName(attribute) + label;
            }
            case ONE_TO_MANY -> owner + " \"1\" -- \"0..*\" " + targetName(attribute) + label;
            default -> owner + " \"0..*\" -- \"0..*\" " + targetName(attribute) + label;
        });
    }

    private static boolean isInverseSide(Attribute<?, ?> attribute) {
        if (!(attribute.getJavaMember() instanceof AnnotatedElement member)) {
            return false;
        }
        OneToMany oneToMany = member.getAnnotation(OneToMany.class);
        OneToOne oneToOne = member.getAnnotation(OneToOne.class);
        ManyToMany manyToMany = member.getAnnotation(ManyToMany.class);
        return oneToMany != null && !oneToMany.mappedBy().isEmpty()
                || oneToOne != null && !oneToOne.mappedBy().isEmpty()
                || manyToMany != null && !manyToMany.mappedBy().isEmpty();
    }

    private static String targetName(Attribute<?, ?> attribute) {
        if (attribute instanceof PluralAttribute<?, ?, ?> plural) {
            return plural.getElementType().getJavaType().getSimpleName();
        }
        return attribute.getJavaType().getSimpleName();
    }

    private static void write(String fileName, StringBuilder puml) throws IOException {
        puml.append("\n@enduml\n");
        Files.createDirectories(OUTPUT_DIR);
        Files.writeString(OUTPUT_DIR.resolve(fileName), puml, StandardCharsets.UTF_8);
    }
}
