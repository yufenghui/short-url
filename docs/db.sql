CREATE TABLE `url_map`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT,
    `code`        varchar(10)   DEFAULT NULL COMMENT '短链码',
    `short_url`   varchar(100)  DEFAULT NULL COMMENT '短链接',
    `origin_url`  varchar(1000) DEFAULT NULL COMMENT '原始链接',
    `create_time` datetime      DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `index_code` (`code`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 11
  DEFAULT CHARSET = utf8mb4;