-- SQLINES DEMO *** le_ci;
SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `tbl_Notification`;
DROP TABLE IF EXISTS `tbl_Expense`;
DROP TABLE IF EXISTS `tbl_Budget`;
DROP TABLE IF EXISTS `tbl_ItineraryItem`;
DROP TABLE IF EXISTS `tbl_Trip`;
DROP TABLE IF EXISTS `tbl_Account`;
DROP TABLE IF EXISTS `tbl_Customer`;
DROP TABLE IF EXISTS `tbl_WeatherForecast`;
DROP TABLE IF EXISTS `tbl_Location`;
DROP TABLE IF EXISTS `tbl_Destination`;

CREATE TABLE `tbl_Customer` (
    `customerId` INT(10) NOT NULL AUTO_INCREMENT,
    `fullName` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(20) NOT NULL,
    `address` VARCHAR(100) NULL,
    `dob` DATE NULL,
    PRIMARY KEY (`customerId`),
    UNIQUE (`email`),
    UNIQUE (`phone`)
);

CREATE TABLE `tbl_Account` (
    `accountId` INT(10) NOT NULL AUTO_INCREMENT,
    `userName` VARCHAR(100) NOT NULL,
    `passwordHash` VARCHAR(255) NOT NULL,
    `socialProvider` VARCHAR(100) NULL,
    `tbl_CustomercustomerId` INT(10) NOT NULL,
    PRIMARY KEY (`accountId`),
    UNIQUE (`userName`),
    CONSTRAINT `fk_Account_Customer`
        FOREIGN KEY (`tbl_CustomercustomerId`)
        REFERENCES `tbl_Customer` (`customerId`)
        ON DELETE CASCADE
);

CREATE TABLE `tbl_Trip` (
    `tripId` INT(10) NOT NULL AUTO_INCREMENT,
    `tripName` VARCHAR(100) NOT NULL,
    `startDate` DATE NOT NULL,
    `endDate` DATE NOT NULL,
    `tbl_CustomercustomerId` INT(10) NOT NULL,
    PRIMARY KEY (`tripId`),
    CONSTRAINT `fk_Trip_Customer`
        FOREIGN KEY (`tbl_CustomercustomerId`)
        REFERENCES `tbl_Customer` (`customerId`)
        ON DELETE CASCADE
);

CREATE TABLE `tbl_Budget` (
    `budgetId` INT(10) NOT NULL AUTO_INCREMENT,
    `totalAmount` DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    `currency` VARCHAR(3) NOT NULL DEFAULT 'VND',
    `tbl_TriptripId` INT(10) NOT NULL,
    PRIMARY KEY (`budgetId`),
    UNIQUE (`tbl_TriptripId`),
    CONSTRAINT `fk_Budget_Trip`
        FOREIGN KEY (`tbl_TriptripId`)
        REFERENCES `tbl_Trip` (`tripId`)
        ON DELETE CASCADE
);

CREATE TABLE `tbl_Expense` (
    `expenseId` INT(10) NOT NULL AUTO_INCREMENT,
    `amount` DECIMAL(12, 2) NOT NULL,
    `category` VARCHAR(100) NOT NULL,
    `date` DATE NOT NULL,
    `tbl_BudgetbudgetId` INT(10) NOT NULL,
    PRIMARY KEY (`expenseId`),
    CONSTRAINT `fk_Expense_Budget`
        FOREIGN KEY (`tbl_BudgetbudgetId`)
        REFERENCES `tbl_Budget` (`budgetId`)
        ON DELETE CASCADE
);

CREATE TABLE `tbl_Destination` (
    `destinationId` INT(10) NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL,
    `description` VARCHAR(255) NULL,
    PRIMARY KEY (`destinationId`)
);

CREATE TABLE `tbl_Location` (
    `tbl_DestinationdestinationId` INT(10) NOT NULL,
    `latitude` DECIMAL(9, 6) NOT NULL,
    `longitude` DECIMAL(9, 6) NOT NULL,
    `description` VARCHAR(255) NULL,
    PRIMARY KEY (`tbl_DestinationdestinationId`),
    CONSTRAINT `fk_Location_Destination`
        FOREIGN KEY (`tbl_DestinationdestinationId`)
        REFERENCES `tbl_Destination` (`destinationId`)
        ON DELETE CASCADE
);

CREATE TABLE `tbl_ItineraryItem` (
    `tbl_TriptripId` INT(10) NOT NULL,
    `tbl_DestinationdestinationId` INT(10) NOT NULL,
    `arrivalDate` DATE NOT NULL,
    `departureDate` DATE NOT NULL,
    `notes` VARCHAR(255) NULL,
    PRIMARY KEY (`tbl_TriptripId`, `tbl_DestinationdestinationId`),
    CONSTRAINT `fk_Itinerary_Trip`
        FOREIGN KEY (`tbl_TriptripId`)
        REFERENCES `tbl_Trip` (`tripId`)
        ON DELETE CASCADE,
    CONSTRAINT `fk_Itinerary_Destination`
        FOREIGN KEY (`tbl_DestinationdestinationId`)
        REFERENCES `tbl_Destination` (`destinationId`)
        ON DELETE CASCADE
);

CREATE TABLE `tbl_WeatherForecast` (
    `forecastId` INT(10) NOT NULL AUTO_INCREMENT,
    `date` DATE NOT NULL,
    `temperature` DECIMAL(5, 2) NOT NULL,
    `condition` VARCHAR(100) NULL,
    `tbl_DestinationdestinationId` INT(10) NOT NULL,
    PRIMARY KEY (`forecastId`),
    CONSTRAINT `fk_Forecast_Destination`
        FOREIGN KEY (`tbl_DestinationdestinationId`)
        REFERENCES `tbl_Destination` (`destinationId`)
        ON DELETE CASCADE
);

CREATE TABLE `tbl_Notification` (
    `notificationId` INT(10) NOT NULL AUTO_INCREMENT,
    `message` VARCHAR(255) NOT NULL,
    `timestamp` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `isRead` TINYINT(1) NOT NULL DEFAULT 0,
    `tbl_CustomercustomerId` INT(10) NOT NULL,
    PRIMARY KEY (`notificationId`),
    CONSTRAINT `fk_Notification_Customer`
        FOREIGN KEY (`tbl_CustomercustomerId`)
        REFERENCES `tbl_Customer` (`customerId`)
        ON DELETE CASCADE
);

SET FOREIGN_KEY_CHECKS=1;