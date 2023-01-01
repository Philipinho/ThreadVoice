CREATE DATABASE IF NOT EXISTS `threadvoice`;
USE `threadvoice`;

CREATE TABLE IF NOT EXISTS `tweet_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(20) NOT NULL,
  `mention_id`  varchar(255) DEFAULT NULL,
  `thread_id`  varchar(255) DEFAULT NULL,
  `voice_url` varchar(500) DEFAULT NULL,
  `thread_author` varchar(20) DEFAULT NULL,
  `thread_excerpt` varchar(500) DEFAULT NULL,
  `thread_language` varchar(255) DEFAULT NULL,
  `time_saved` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY(`mention_id`)
) ENGINE=InnoDB;