ALTER TABLE blog_user
    ADD CONSTRAINT chk_blog_user_status CHECK (status IN (0, 1));

ALTER TABLE blog_article
    ADD KEY idx_article_author_status_update_time (author_id, status, update_time);
