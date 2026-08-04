CREATE TABLE blog_user (
                           id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                           username VARCHAR(50) NOT NULL COMMENT '用户名',
                           password VARCHAR(100) NOT NULL COMMENT '加密后的密码',
                           nickname VARCHAR(50) NOT NULL COMMENT '昵称',
                           status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常，0禁用',
                           create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                               ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',

                           PRIMARY KEY (id),
                           UNIQUE KEY uk_blog_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客用户表';

USE blog;

CREATE TABLE blog_article (
                              id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT
        COMMENT '文章ID',

                              title VARCHAR(200) NOT NULL
                                  COMMENT '文章标题',

                              summary VARCHAR(500) NOT NULL DEFAULT ''
                                  COMMENT '文章摘要',

                              content LONGTEXT NOT NULL
                                  COMMENT '文章正文',

                              author_id BIGINT UNSIGNED NOT NULL
        COMMENT '作者用户ID',

                              view_count BIGINT UNSIGNED NOT NULL DEFAULT 0
        COMMENT '浏览次数',

                              status TINYINT NOT NULL DEFAULT 0
                                  COMMENT '状态：0草稿，1已发布，2已删除',

                              create_time DATETIME NOT NULL
                                                            DEFAULT CURRENT_TIMESTAMP
                                  COMMENT '创建时间',

                              update_time DATETIME NOT NULL
                                                            DEFAULT CURRENT_TIMESTAMP
                                  ON UPDATE CURRENT_TIMESTAMP
        COMMENT '修改时间',

                              PRIMARY KEY (id),

                              KEY idx_article_author_id (author_id),

                              KEY idx_article_status_create_time (
        status,
        create_time
    ),

                              CONSTRAINT fk_article_author
                                  FOREIGN KEY (author_id)
                                      REFERENCES blog_user (id)
                                      ON DELETE RESTRICT
                                      ON UPDATE RESTRICT,

                              CONSTRAINT chk_blog_article_status
                                  CHECK (status IN (0, 1, 2))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci
  COMMENT='博客文章表';

CREATE TABLE blog_comment (
                              id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT
        COMMENT '评论ID',

                              content VARCHAR(1000) NOT NULL
                                  COMMENT '评论内容',

                              user_id BIGINT UNSIGNED NOT NULL
        COMMENT '评论用户ID',

                              article_id BIGINT UNSIGNED NOT NULL
        COMMENT '所属文章ID',

                              status TINYINT NOT NULL DEFAULT 1
                                  COMMENT '状态：0隐藏，1正常，2已删除',

                              create_time DATETIME NOT NULL
                                                      DEFAULT CURRENT_TIMESTAMP
                                  COMMENT '创建时间',

                              PRIMARY KEY (id),

                              KEY idx_comment_user_id (user_id),

                              KEY idx_comment_article_status_time (
        article_id,
        status,
        create_time
    ),

                              CONSTRAINT fk_comment_user
                                  FOREIGN KEY (user_id)
                                      REFERENCES blog_user (id)
                                      ON DELETE RESTRICT
                                      ON UPDATE RESTRICT,

                              CONSTRAINT fk_comment_article
                                  FOREIGN KEY (article_id)
                                      REFERENCES blog_article (id)
                                      ON DELETE RESTRICT
                                      ON UPDATE RESTRICT,

                              CONSTRAINT chk_blog_comment_status
                                  CHECK (status IN (0, 1, 2))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci
  COMMENT='博客评论表';