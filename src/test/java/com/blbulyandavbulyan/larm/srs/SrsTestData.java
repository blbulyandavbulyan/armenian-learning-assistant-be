package com.blbulyandavbulyan.larm.srs;

import java.util.UUID;

/**
 * Ids and fixture paths shared by the spaced repetition integration tests.
 */
public final class SrsTestData {

    public static final UUID DIALOGUE_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");
    public static final UUID SPEAKER_A_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    public static final UUID SPEAKER_B_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    /** Line 1, speaker A, "Բարեւ ձեզ". */
    public static final UUID LINE_1_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    /** Line 2, speaker B. */
    public static final UUID LINE_2_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    /** Line 3, speaker A, "Ահա, խնդրեմ:". */
    public static final UUID LINE_3_ID = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");

    public static final UUID TEST_USER_ID = UUID.fromString("5a5a5a5a-0000-0000-0000-000000000001");
    public static final UUID LEARNING_ITEM_ID = UUID.fromString("1a1a1a1a-0000-0000-0000-000000000001");
    public static final UUID OTHER_USER_LEARNING_ITEM_ID = UUID.fromString("1a1a1a1a-0000-0000-0000-000000000002");
    public static final UUID CARD_A_ID = UUID.fromString("2b2b2b2b-0000-0000-0000-0000000000aa");
    public static final UUID CARD_B_ID = UUID.fromString("2b2b2b2b-0000-0000-0000-0000000000bb");
    public static final UUID OTHER_USER_CARD_A_ID = UUID.fromString("2b2b2b2b-0000-0000-0000-0000000002aa");
    public static final UUID IN_PROGRESS_SESSION_ID = UUID.fromString("3c3c3c3c-0000-0000-0000-000000000001");
    public static final UUID COMPLETED_SESSION_ID = UUID.fromString("3c3c3c3c-0000-0000-0000-000000000002");
    public static final UUID OTHER_USER_SESSION_ID = UUID.fromString("3c3c3c3c-0000-0000-0000-000000000003");
    public static final UUID FIRST_ATTEMPT_ID = UUID.fromString("4d4d4d4d-0000-0000-0000-000000000001");

    /**
     * Paths of the SQL fixtures; usable in {@code @Sql}.
     */
    public interface Fixtures {
        String DIALOGUE = "/sql-test-scripts/insert-dialogue.sql";
        String TEST_USER = "/sql-test-scripts/srs/insert-test-user.sql";
        String ACTIVE_LEARNING_ITEM = "/sql-test-scripts/srs/insert-active-learning-item.sql";
        String EXCLUDED_LEARNING_ITEM = "/sql-test-scripts/srs/insert-excluded-learning-item.sql";
        String EXCLUDE_ACTIVE_LEARNING_ITEM = "/sql-test-scripts/srs/exclude-active-learning-item.sql";
        String OTHER_USER_LEARNING_ITEM = "/sql-test-scripts/srs/insert-other-user-learning-item.sql";
        String IN_PROGRESS_SESSION = "/sql-test-scripts/srs/insert-in-progress-session.sql";
        String FIRST_LINE_ATTEMPTS = "/sql-test-scripts/srs/insert-first-line-attempts.sql";
        String LAST_LINE_ATTEMPT = "/sql-test-scripts/srs/insert-last-line-attempt.sql";
        String COMPLETED_SESSION = "/sql-test-scripts/srs/insert-completed-session.sql";
        String OTHER_USER_COMPLETED_SESSION = "/sql-test-scripts/srs/insert-other-user-completed-session.sql";
        String OTHER_USER_SESSION = "/sql-test-scripts/srs/insert-other-user-session.sql";
    }

    private SrsTestData() {
    }
}
