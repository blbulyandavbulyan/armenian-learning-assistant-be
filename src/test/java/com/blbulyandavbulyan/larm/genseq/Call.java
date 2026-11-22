package com.blbulyandavbulyan.larm.genseq;

import java.util.List;
import java.util.Map;

/**
 * A node of the call tree reconstructed from spans.
 *
 * @param kind       what kind of participant this call targets
 * @param label      main text of the call (method, SQL, endpoint)
 * @param detail     secondary text, empty when absent
 * @param attributes extra facts such as {@code status} or {@code count}
 * @param children   nested calls in start order
 */
public record Call(Kind kind, String label, String detail, Map<String, String> attributes, List<Call> children) {

    /**
     * Participant category of a call.
     */
    public enum Kind { REQUEST, COMPONENT, SQL, PIPER, GEMINI_CHAT, GEMINI_EMBED }
}
