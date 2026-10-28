-- TODO: Add optional deterministic test users and database fixtures.
-- TODO: Use only test data; do not put real credentials here.
INSERT INTO users (
    id,
    login,
    password_hash,
    full_name,
    role,
    block,
    room_number,
    created_at
)
VALUES (
    '22222222-2222-2222-2222-222222222222',
    'fixtureuser',
    'test-only-password-hash',
    'Fixture Test User',
    'RESIDENT',
    '11111111-1111-1111-1111-111111111111',
    '101',
    TIMESTAMPTZ '2026-01-01 12:00:00+00'
)
ON CONFLICT (login) DO NOTHING;
