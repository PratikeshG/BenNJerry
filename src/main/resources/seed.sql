-- Seed the MySQL databases used by the managed integrations platform.

-- Create syntax for TABLE 'tntfireworks_locations'
CREATE TABLE `tntfireworks_locations` (
  `locationNumber` varchar(20) NOT NULL,
  `addressNumber` varchar(20) DEFAULT NULL,
  `name` varchar(40) DEFAULT NULL,
  `address` varchar(50) DEFAULT NULL,
  `city` varchar(40) DEFAULT NULL,
  `state` varchar(30) DEFAULT NULL,
  `zip` varchar(20) DEFAULT NULL,
  `county` varchar(40) DEFAULT NULL,
  `mktPlan` varchar(20) DEFAULT '',
  `legal` varchar(11) DEFAULT NULL,
  `disc` varchar(11) DEFAULT NULL,
  `rbu` varchar(11) DEFAULT NULL,
  `bp` varchar(11) DEFAULT NULL,
  `co` varchar(20) DEFAULT NULL,
  `saNum` varchar(20) DEFAULT NULL,
  `saName` varchar(30) DEFAULT NULL,
  `custNum` varchar(20) DEFAULT NULL,
  `custName` varchar(50) DEFAULT NULL,
  `season` varchar(20) DEFAULT NULL,
  `year` varchar(11) DEFAULT NULL,
  `machineType` varchar(11) DEFAULT NULL,
  `deployment` varchar(20) DEFAULT '',
  `sqDashboardEmail` varchar(40) DEFAULT NULL,
  PRIMARY KEY (`locationNumber`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Create syntax for TABLE 'tntfireworks_marketing_plans'
CREATE TABLE `tntfireworks_marketing_plans` (
  `mktPlan` varchar(20) NOT NULL DEFAULT '',
  `itemNumber` varchar(20) NOT NULL DEFAULT '',
  `cat` varchar(20) DEFAULT NULL,
  `category` varchar(40) DEFAULT '',
  `itemDescription` varchar(50) DEFAULT NULL,
  `casePacking` varchar(20) DEFAULT NULL,
  `unitPrice` varchar(20) DEFAULT NULL,
  `pricingUOM` varchar(20) DEFAULT NULL,
  `suggestedPrice` varchar(20) NOT NULL DEFAULT '',
  `sellingUOM` varchar(20) DEFAULT NULL,
  `upc` varchar(20) DEFAULT NULL,
  `netItem` varchar(20) DEFAULT NULL,
  `expiredDate` varchar(20) DEFAULT NULL,
  `effectiveDate` varchar(20) DEFAULT NULL,
  `bogo` varchar(20) DEFAULT NULL,
  `itemNum3` varchar(20) DEFAULT NULL,
  `currency` varchar(20) DEFAULT NULL,
  `halfOff` varchar(20) DEFAULT NULL,
  `sellingPrice` varchar(20) NOT NULL DEFAULT '',
  PRIMARY KEY (`mktPlan`,`itemNumber`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Create syntax for TABLE 'tntfireworks_reports_load_number'
CREATE TABLE `tntfireworks_reports_load_number` (
  `reportName` varchar(40) NOT NULL DEFAULT '',
  `count` varchar(11) DEFAULT NULL,
  PRIMARY KEY (`reportName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Create syntax for TABLE 'token'
CREATE TABLE `token` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `deployment` varchar(45) DEFAULT NULL,
  `connectApp` varchar(45) DEFAULT NULL,
  `token` varchar(45) DEFAULT NULL,
  `merchantId` varchar(45) DEFAULT NULL,
  `locationId` varchar(45) DEFAULT NULL,
  `legacy` tinyint(1) NOT NULL DEFAULT '0',
  `expiryDate` varchar(45) DEFAULT NULL,
  `merchantAlias` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Create syntax for TABLE 'vfcorp_deployments'
CREATE TABLE `vfcorp_deployments` (
  `deployment` varchar(20) NOT NULL,
  `deploymentGroup` varchar(20) DEFAULT NULL,
  `storeId` varchar(5) DEFAULT NULL,
  `name` varchar(40) DEFAULT NULL,
  `timeZone` varchar(20) DEFAULT NULL,
  `enablePLU` tinyint(1) DEFAULT NULL,
  `enableTLOG` tinyint(1) DEFAULT NULL,
  `pluPath` varchar(100) DEFAULT NULL,
  `pluFiltered` tinyint(1) DEFAULT NULL,
  `tlogPath` varchar(100) DEFAULT NULL,
  `tlogRange` int(11) DEFAULT NULL,
  `tlogOffset` int(11) DEFAULT NULL,
  PRIMARY KEY (`deployment`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Create syntax for TABLE 'vfcorp_plu_dept_class'
CREATE TABLE `vfcorp_plu_dept_class` (
  `deployment` varchar(20) DEFAULT NULL,
  `merchantId` varchar(20) DEFAULT NULL,
  `locationId` varchar(20) NOT NULL,
  `deptNumber` varchar(4) NOT NULL,
  `classNumber` varchar(4) NOT NULL,
  `description` varchar(24) DEFAULT NULL,
  `updatedAt` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`locationId`,`deptNumber`,`classNumber`),
  KEY `locationId` (`locationId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Create syntax for TABLE 'vfcorp_plu_items'
CREATE TABLE `vfcorp_plu_items` (
  `deployment` varchar(20) DEFAULT NULL,
  `merchantId` varchar(20) DEFAULT '',
  `locationId` varchar(20) NOT NULL,
  `itemNumber` varchar(24) NOT NULL DEFAULT '',
  `deptNumber` varchar(4) DEFAULT NULL,
  `classNumber` varchar(4) DEFAULT NULL,
  `styleNumber` varchar(24) DEFAULT NULL,
  `activateDate` varchar(8) DEFAULT NULL,
  `deactivateDate` varchar(8) DEFAULT NULL,
  `description` varchar(24) DEFAULT NULL,
  `alternateDescription` varchar(40) DEFAULT NULL,
  `retailPrice` varchar(10) DEFAULT NULL,
  `originalPrice` varchar(10) DEFAULT NULL,
  `salePrice` varchar(10) DEFAULT NULL,
  `dateSaleBegins` varchar(8) DEFAULT NULL,
  `dateSaleEnds` varchar(8) DEFAULT NULL,
  `timeSaleBegins` varchar(8) DEFAULT NULL,
  `timeSaleEnds` varchar(8) DEFAULT NULL,
  `updatedAt` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`locationId`,`itemNumber`),
  KEY `locationId` (`locationId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Create syntax for TABLE 'vfcorp_plu_sale_events'
CREATE TABLE `vfcorp_plu_sale_events` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `deployment` varchar(20) DEFAULT NULL,
  `merchantId` varchar(20) DEFAULT NULL,
  `locationId` varchar(20) NOT NULL,
  `itemNumber` varchar(24) NOT NULL,
  `salePrice` varchar(10) DEFAULT NULL,
  `dateSaleBegins` varchar(8) NOT NULL,
  `dateSaleEnds` varchar(8) NOT NULL,
  `timeSaleBegins` varchar(8) DEFAULT NULL,
  `timeSaleEnds` varchar(8) DEFAULT NULL,
  `updatedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_sales` (`locationId`,`itemNumber`,`salePrice`,`dateSaleBegins`,`dateSaleEnds`),
  KEY `locationId` (`locationId`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- Create syntax for TABLE 'vfcorp_preferred_customer_counter'
CREATE TABLE `vfcorp_preferred_customer_counter` (
  `deployment` varchar(30) NOT NULL DEFAULT '',
  `storeId` varchar(5) NOT NULL DEFAULT '',
  `registerId` varchar(3) NOT NULL DEFAULT '',
  `nextPreferredCustomerNumber` int(11) NOT NULL,
  `updatedAt` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `deployment` (`deployment`,`storeId`,`registerId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;