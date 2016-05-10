-- Seed the MySQL databases used by the managed integrations platform.

DROP DATABASE IF EXISTS `development`;
CREATE DATABASE `development`;
DROP DATABASE IF EXISTS `staging`;
CREATE DATABASE `staging`;

USE `staging`;

DROP TABLE IF EXISTS `token`;

CREATE TABLE `token` (
  `id` INTEGER NULL AUTO_INCREMENT DEFAULT NULL,
  `deployment` VARCHAR(45) NULL DEFAULT NULL,
  `connectApp` VARCHAR(45) NULL DEFAULT NULL,
  `token` VARCHAR(45) NULL DEFAULT NULL,
  `merchantId` VARCHAR(45) NULL DEFAULT NULL,
  `locationId` VARCHAR(45) NULL DEFAULT NULL,
  `legacy` BOOLEAN NOT NULL DEFAULT FALSE,
  `expiryDate` VARCHAR(45) NULL DEFAULT NULL,
  `merchantName` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `vfcorp-transaction-number` (
  `lastTransactionNumber` INTEGER,
  PRIMARY KEY (`lastTransactionNumber`)
);

USE `development`;

DROP TABLE IF EXISTS `token`;

CREATE TABLE `token` (
  `id` INTEGER NULL AUTO_INCREMENT DEFAULT NULL,
  `deployment` VARCHAR(45) NULL DEFAULT NULL,
  `connectApp` VARCHAR(45) NULL DEFAULT NULL,
  `token` VARCHAR(45) NULL DEFAULT NULL,
  `merchantId` VARCHAR(45) NULL DEFAULT NULL,
  `locationId` VARCHAR(45) NULL DEFAULT NULL,
  `legacy` BOOLEAN NOT NULL DEFAULT FALSE,
  `expiryDate` VARCHAR(45) NULL DEFAULT NULL,
  `merchantName` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `vfcorp-transaction-number` (
  `lastTransactionNumber` INTEGER,
  PRIMARY KEY (`lastTransactionNumber`)
);

-- Staging data
-- INSERT INTO `token` (`deployment`,`connectApp`,`token`,`merchantId`) VALUES ("urbanspace","vYMYVXtyvWU0JkTDD_EYLw","daT6X7kSZj38L70iMELIsw","7PZ8TKDZB8B54");
-- INSERT INTO `token` (`deployment`,`connectApp`,`token`,`merchantId`) VALUES ("urbanspace","vYMYVXtyvWU0JkTDD_EYLw","lXZLAyxqKCNXy3UoYTVQ2w","3ABWWE16MAK89");
-- INSERT INTO `token` (`deployment`,`connectApp`,`token`,`merchantId`) VALUES ("urbanspace","vYMYVXtyvWU0JkTDD_EYLw","z4l1RRiOMCRoICv01pFuNg","BTMHQ5RW1A6EA");
-- INSERT INTO `token` (`deployment`,`connectApp`,`token`,`merchantId`,`locationId`) VALUES ("vfcorp","vYMYVXtyvWU0JkTDD_EYLw","s_CHLS8yYTsu3Ts9Rngd2Q","5PEBESGVQ95BC","D67SWP5DZ9AWG");
-- INSERT INTO `token` (`deployment`,`connectApp`,`token`,`merchantId`,`locationId`) VALUES ("vfcorp","sq0idp-xD8FBJLi38w3CdUBsu2hlQ","sq0atp-eWyKq9VkPuj-ZKuwi6XYew","DS7QMM3ETABZM","E8V3AF2CWMNWV"); -- VFCorp test location
-- INSERT INTO `token` (`deployment`,`connectApp`,`token`,`merchantId`,`locationId`) VALUES ("vfcorp","vYMYVXtyvWU0JkTDD_EYLw","sq0ats-hqRgaU2PkvwxBOqIcdfGYg","me","D67SWP5DZ9AWG"); -- colinlam+eldmmaster
-- INSERT INTO `token` (`deployment`,`connectApp`,`token`,`merchantId`,`locationId`) VALUES ("urbanspace","vYMYVXtyvWU0JkTDD_EYLw","sq0ats-hqRgaU2PkvwxBOqIcdfGYg","me","D67SWP5DZ9AWG"); -- colinlam+eldmmaster
-- INSERT INTO `vfcorp-transaction-number` (`lastTransactionNumber`) VALUES (4);