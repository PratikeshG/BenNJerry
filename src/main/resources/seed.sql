-- Seed the MySQL databases used by the managed integrations platform.

DROP DATABASE IF EXISTS `development`;
CREATE DATABASE `development`;
USE `development`;
 
DROP TABLE IF EXISTS `deployment`;

CREATE TABLE `deployment` (
  `id` INTEGER NULL AUTO_INCREMENT DEFAULT NULL,
  `name` VARCHAR(45) NULL DEFAULT NULL,
  `connectApp` VARCHAR(45) NULL DEFAULT NULL,
  `appSecret` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `token`;

CREATE TABLE `token` (
  `id` INTEGER NULL AUTO_INCREMENT DEFAULT NULL,
  `token` VARCHAR(45) NULL DEFAULT NULL,
  `date` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `merchant`;

CREATE TABLE `merchant` (
  `id` INTEGER NULL AUTO_INCREMENT DEFAULT NULL,
  `merchantId` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `location`;

CREATE TABLE `location` (
  `id` INTEGER NULL AUTO_INCREMENT DEFAULT NULL,
  `locationId` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `connect_scope`;

CREATE TABLE `connect_scope` (
  `id` INTEGER NULL AUTO_INCREMENT DEFAULT NULL,
  `scope` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
);

DROP TABLE IF EXISTS `payload`;

CREATE TABLE `payload` (
  `id` INTEGER NULL AUTO_INCREMENT DEFAULT NULL,
  `deploymentId` INTEGER NULL DEFAULT NULL,
  `tokenId` INTEGER NULL DEFAULT NULL,
  `merchantId` INTEGER NULL DEFAULT NULL,
  `locationId` INTEGER NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (deploymentId) REFERENCES deployment(id),
  FOREIGN KEY (tokenId) REFERENCES token(id),
  FOREIGN KEY (merchantId) REFERENCES merchant(id),
  FOREIGN KEY (locationId) REFERENCES location(id)
);

DROP TABLE IF EXISTS `scope`;

CREATE TABLE `scope` (
  `id` INTEGER NULL AUTO_INCREMENT DEFAULT NULL,
  `deploymentId` INTEGER NULL DEFAULT NULL,
  `scopeId` INTEGER NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  FOREIGN KEY (deploymentId) REFERENCES deployment(id),
  FOREIGN KEY (scopeId) REFERENCES scope(id)
);

INSERT INTO `connect_scope` (scope) VALUES ("MERCHANT_PROFILE_READ");
INSERT INTO `connect_scope` (scope) VALUES ("PAYMENTS_READ");
INSERT INTO `connect_scope` (scope) VALUES ("PAYMENTS_WRITE");
INSERT INTO `connect_scope` (scope) VALUES ("SETTLEMENTS_READ");
INSERT INTO `connect_scope` (scope) VALUES ("BANK_ACCOUNTS_READ");
INSERT INTO `connect_scope` (scope) VALUES ("ITEMS_READ");
INSERT INTO `connect_scope` (scope) VALUES ("ITEMS_WRITE");
INSERT INTO `connect_scope` (scope) VALUES ("ORDERS_READ");
INSERT INTO `connect_scope` (scope) VALUES ("ORDERS_WRITE");
INSERT INTO `connect_scope` (scope) VALUES ("EMPLOYEES_READ");
INSERT INTO `connect_scope` (scope) VALUES ("EMPLOYEES_WRITE");
INSERT INTO `connect_scope` (scope) VALUES ("TIMECARDS_READ");
INSERT INTO `connect_scope` (scope) VALUES ("TIMECARDS_WRITE");