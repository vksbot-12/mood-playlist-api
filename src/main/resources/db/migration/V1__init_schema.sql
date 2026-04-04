CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE social_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    provider VARCHAR(30) NOT NULL,
    provider_user_id VARCHAR(200) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(provider, provider_user_id)
);

CREATE TABLE usage_quotas (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    free_remaining INT NOT NULL DEFAULT 10,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    status VARCHAR(30) NOT NULL,
    started_at TIMESTAMP,
    expires_at TIMESTAMP,
    provider VARCHAR(30),
    external_reference VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE mood_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    mood_text TEXT NOT NULL,
    mood_summary VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE playlist_recommendations (
    id BIGSERIAL PRIMARY KEY,
    mood_log_id BIGINT NOT NULL REFERENCES mood_logs(id) ON DELETE CASCADE,
    rank_no INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    reason TEXT NOT NULL,
    emotion_link TEXT NOT NULL,
    youtube_query VARCHAR(255),
    youtube_url TEXT,
    youtube_music_supported BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(mood_log_id, rank_no)
);

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    replaced_by_token_hash VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    revoked_at TIMESTAMP,
    reuse_detected BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE share_histories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    mood_log_id BIGINT NOT NULL REFERENCES mood_logs(id),
    channel VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_mood_logs_user_created ON mood_logs(user_id, created_at);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
