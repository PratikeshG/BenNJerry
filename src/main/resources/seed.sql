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

-- Staging data
INSERT INTO `token` (`deployment`,`connectApp`,`token`,`merchantId`) VALUES ("urbanspace","vYMYVXtyvWU0JkTDD_EYLw","daT6X7kSZj38L70iMELIsw","7PZ8TKDZB8B54");
INSERT INTO `token` (`deployment`,`connectApp`,`token`,`merchantId`) VALUES ("urbanspace","vYMYVXtyvWU0JkTDD_EYLw","lXZLAyxqKCNXy3UoYTVQ2w","3ABWWE16MAK89");
INSERT INTO `token` (`deployment`,`connectApp`,`token`,`merchantId`) VALUES ("urbanspace","vYMYVXtyvWU0JkTDD_EYLw","z4l1RRiOMCRoICv01pFuNg","BTMHQ5RW1A6EA");