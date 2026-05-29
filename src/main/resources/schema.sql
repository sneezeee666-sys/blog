CREATE TABLE IF NOT EXISTS `user` (
                                      `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `email` VARCHAR(100),
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS `category` (
                                          `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          `name` VARCHAR(50) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS `article` (
                                         `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         `title` VARCHAR(200) NOT NULL,
    `content` TEXT NOT NULL,
    `summary` VARCHAR(500),
    `category_id` BIGINT,
    `user_id` BIGINT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS `comment` (
                                         `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         `article_id` BIGINT NOT NULL,
                                         `user_id` BIGINT NOT NULL,
                                         `content` TEXT NOT NULL,
                                         `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);