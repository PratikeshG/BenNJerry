-- Seed the MySQL databases used by the managed integrations platform.

DROP DATABASE IF EXISTS `development`;
CREATE DATABASE `development`;
USE `development`;

DROP TABLE IF EXISTS `token`;

CREATE TABLE `token` (
  `id` INTEGER NULL AUTO_INCREMENT DEFAULT NULL,
  `deployment` VARCHAR(45) NULL DEFAULT NULL,
  `connectApp` VARCHAR(45) NULL DEFAULT NULL,
  `token` VARCHAR(45) NULL DEFAULT NULL,
  `merchantId` VARCHAR(45) NULL DEFAULT NULL,
  `expiryDate` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
);