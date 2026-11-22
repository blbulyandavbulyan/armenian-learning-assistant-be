INSERT INTO exercise_sessions (id, learning_card_id, user_id, aggregate_score, started_at, completed_at)
VALUES ('3c3c3c3c-0000-0000-0000-000000000002', '2b2b2b2b-0000-0000-0000-0000000000bb',
        '5a5a5a5a-0000-0000-0000-000000000001', 80, '2026-08-14T09:00:00Z', '2026-08-14T09:10:00Z');

INSERT INTO exercise_attempts (id, session_id, content_sub_item_id, answer_type, answer_text, used_hint,
                               raw_quality, quality, correct, evaluated_at)
VALUES ('4d4d4d4d-0000-0000-0000-000000000004', '3c3c3c3c-0000-0000-0000-000000000002',
        'dddddddd-dddd-dddd-dddd-dddddddddddd', 'TEXT', 'Բարեւ ձեզ, խնդրում եմ մեկ հայ', FALSE, 4, 4, FALSE,
        '2026-08-14T09:05:00Z');
