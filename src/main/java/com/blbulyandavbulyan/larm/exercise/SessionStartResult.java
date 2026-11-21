package com.blbulyandavbulyan.larm.exercise;

/**
 * Started or resumed session; {@code created} is false when an open session was returned.
 */
public record SessionStartResult(SessionResource session, boolean created) {
}
