package com.blbulyandavbulyan.larm.genseq;

/**
 * Span names and attribute keys the sequence diagram generator relies on.
 */
public final class SpanNames {
    public static final String ROUTE_KEY = "uri";
    public static final String STATUS_KEY = "status";

    public static final String JDBC_SPAN_NAME = "query";
    public static final String JDBC_SQL_KEY = "jdbc.query[0]";

    public static final String COMPONENT_SPAN = "larm.component";
    public static final String EMBEDDING_SPAN = "gen_ai.embedding";
    public static final String NAMESPACE_KEY = "code.namespace";
    public static final String FUNCTION_KEY = "code.function";

    private SpanNames() {
    }
}
