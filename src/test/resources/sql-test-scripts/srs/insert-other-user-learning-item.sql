INSERT INTO users (id, issuer, subject)
VALUES ('5a5a5a5a-0000-0000-0000-000000000002', 'https://test-issuer.com', 'other-subject');

INSERT INTO learning_items (id, user_id, content_item_id, content_item_type, enrolled_at, excluded_at)
VALUES ('1a1a1a1a-0000-0000-0000-000000000002', '5a5a5a5a-0000-0000-0000-000000000002',
        '99999999-9999-9999-9999-999999999999', 'DIALOGUE', '2026-08-02T09:00:00Z', NULL);

INSERT INTO learning_cards (id, learning_item_id, exercise_type, variant_id, next_review_at, interval_days,
                            ease_factor, repetition_count, created_at, updated_at)
VALUES ('2b2b2b2b-0000-0000-0000-0000000002aa', '1a1a1a1a-0000-0000-0000-000000000002', 'TYPING',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '2026-08-02T09:00:00Z', 0, 2.5, 0,
        '2026-08-02T09:00:00Z', '2026-08-02T09:00:00Z'),
       ('2b2b2b2b-0000-0000-0000-0000000002bb', '1a1a1a1a-0000-0000-0000-000000000002', 'TYPING',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '2026-08-02T09:00:00Z', 0, 2.5, 0,
        '2026-08-02T09:00:00Z', '2026-08-02T09:00:00Z');
