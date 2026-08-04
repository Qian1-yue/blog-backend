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