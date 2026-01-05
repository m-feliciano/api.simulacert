CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    attempt_id UUID NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_reviews_users FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_reviews_attempts FOREIGN KEY (attempt_id) REFERENCES attempts(id),
    CONSTRAINT uk_user_attempt UNIQUE (user_id, attempt_id)
);

CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_reviews_attempt_id ON reviews(attempt_id);

