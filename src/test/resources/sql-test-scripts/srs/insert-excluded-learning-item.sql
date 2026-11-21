INSERT INTO learning_items (id, user_id, content_item_id, content_item_type, enrolled_at, excluded_at)
VALUES ('1a1a1a1a-0000-0000-0000-000000000001', '5a5a5a5a-0000-0000-0000-000000000001',
        '99999999-9999-9999-9999-999999999999', 'DIALOGUE', '2026-08-01T09:00:00Z', '2026-08-10T12:00:00Z');

INSERT INTO learning_cards (id, learning_item_id, exercise_type, variant_id, next_review_at, interval_days,
                            ease_factor, repetition_count, created_at, updated_at)
VALUES ('2b2b2b2b-0000-0000-0000-0000000000aa', '1a1a1a1a-0000-0000-0000-000000000001', 'TYPING',
        'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '2026-08-14T00:00:00Z', 6, 2.5, 2,
        '2026-08-01T09:00:00Z', '2026-08-08T00:00:00Z'),
       ('2b2b2b2b-0000-0000-0000-0000000000bb', '1a1a1a1a-0000-0000-0000-000000000001', 'TYPING',
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '2026-08-20T09:10:00Z', 6, 2.6, 2,
        '2026-08-01T09:00:00Z', '2026-08-14T09:10:00Z');
