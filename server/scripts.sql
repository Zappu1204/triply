-- Trip Planning Application Database Schema
SET FOREIGN_KEY_CHECKS=0;

-- Drop existing tables
DROP TABLE IF EXISTS `tbl_ChatMessage`;
DROP TABLE IF EXISTS `tbl_ChatThread`;
DROP TABLE IF EXISTS `tbl_TripAttraction`;
DROP TABLE IF EXISTS `tbl_TripHotel`;
DROP TABLE IF EXISTS `tbl_TripFlight`;
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
DROP TABLE IF EXISTS `tbl_City`;
DROP TABLE IF EXISTS `tbl_Region`;

-- ==================== CUSTOMER & AUTHENTICATION ====================

CREATE TABLE `tbl_Customer` (
    `customerId` INT(10) NOT NULL AUTO_INCREMENT,
    `fullName` VARCHAR(100) NOT NULL,
    `email` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(20) NULL,
    `address` VARCHAR(100) NULL,
    `dob` DATE NULL,
    PRIMARY KEY (`customerId`),
    UNIQUE (`email`),
    UNIQUE (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tbl_Account` (
    `accountId` INT(10) NOT NULL AUTO_INCREMENT,
    `userName` VARCHAR(100) NOT NULL,
    `passwordHash` VARCHAR(255) NOT NULL,
    `socialProvider` VARCHAR(100) NULL COMMENT 'LOCAL, GOOGLE',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'USER, ADMIN',
    `tbl_CustomercustomerId` INT(10) NOT NULL,
    PRIMARY KEY (`accountId`),
    UNIQUE (`userName`),
    CONSTRAINT `fk_Account_Customer`
        FOREIGN KEY (`tbl_CustomercustomerId`)
        REFERENCES `tbl_Customer` (`customerId`)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== LOCATION HIERARCHY ====================

CREATE TABLE `tbl_Region` (
    `regionId` INT(10) NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `description` TEXT NULL,
    `createdAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updatedAt` TIMESTAMP NULL,
    PRIMARY KEY (`regionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tbl_City` (
    `cityId` INT(10) NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `tbl_RegionregionId` INT(10) NULL,
    `latitude` DECIMAL(9, 6) NULL COMMENT 'GPS Latitude',
    `longitude` DECIMAL(9, 6) NULL COMMENT 'GPS Longitude',
    `description` TEXT NULL,
    `createdAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updatedAt` TIMESTAMP NULL,
    PRIMARY KEY (`cityId`),
    CONSTRAINT `fk_City_Region`
        FOREIGN KEY (`tbl_RegionregionId`)
        REFERENCES `tbl_Region` (`regionId`)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tbl_Destination` (
    `destinationId` INT(10) NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `address` VARCHAR(255) NULL,
    `imgPath` VARCHAR(500) NULL,
    `googleMapUrl` VARCHAR(500) NULL,
    `description` TEXT NULL,
    `tbl_CitycityId` INT(10) NULL,
    `createdAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updatedAt` TIMESTAMP NULL,
    PRIMARY KEY (`destinationId`),
    CONSTRAINT `fk_Destination_City`
        FOREIGN KEY (`tbl_CitycityId`)
        REFERENCES `tbl_City` (`cityId`)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== TRIP PLANNING ====================

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== TRIP COMPONENTS (Flight, Hotel, Attraction) ====================

CREATE TABLE `tbl_TripFlight` (
    `tripFlightId` INT(10) NOT NULL AUTO_INCREMENT,
    `tbl_TriptripId` INT(10) NOT NULL,
    `airline` VARCHAR(100) NULL,
    `flightNumber` VARCHAR(50) NULL,
    `departureAirport` VARCHAR(100) NULL,
    `arrivalAirport` VARCHAR(100) NULL,
    `departureTime` TIMESTAMP NULL,
    `arrivalTime` TIMESTAMP NULL,
    `price` DECIMAL(12, 2) NULL,
    `currency` VARCHAR(10) NULL,
    `bookingUrl` VARCHAR(500) NULL,
    `jsonData` TEXT NULL COMMENT 'Full JSON from SerpAPI',
    `createdAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`tripFlightId`),
    UNIQUE (`tbl_TriptripId`),
    CONSTRAINT `fk_TripFlight_Trip`
        FOREIGN KEY (`tbl_TriptripId`)
        REFERENCES `tbl_Trip` (`tripId`)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tbl_TripHotel` (
    `tripHotelId` INT(10) NOT NULL AUTO_INCREMENT,
    `tbl_TriptripId` INT(10) NOT NULL,
    `hotelName` VARCHAR(200) NULL,
    `address` VARCHAR(500) NULL,
    `checkInDate` DATE NULL,
    `checkOutDate` DATE NULL,
    `pricePerNight` DECIMAL(12, 2) NULL,
    `totalPrice` DECIMAL(12, 2) NULL,
    `currency` VARCHAR(10) NULL,
    `rating` DECIMAL(3, 1) NULL,
    `bookingUrl` VARCHAR(500) NULL,
    `latitude` DECIMAL(9, 6) NULL,
    `longitude` DECIMAL(9, 6) NULL,
    `jsonData` TEXT NULL COMMENT 'Full JSON from SerpAPI',
    `createdAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`tripHotelId`),
    UNIQUE (`tbl_TriptripId`),
    CONSTRAINT `fk_TripHotel_Trip`
        FOREIGN KEY (`tbl_TriptripId`)
        REFERENCES `tbl_Trip` (`tripId`)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tbl_TripAttraction` (
    `tripAttractionId` INT(10) NOT NULL AUTO_INCREMENT,
    `tbl_TriptripId` INT(10) NOT NULL,
    `name` VARCHAR(200) NULL,
    `address` VARCHAR(500) NULL,
    `description` TEXT NULL,
    `visitDate` DATE NULL,
    `estimatedDuration` INT(10) NULL COMMENT 'Duration in minutes',
    `category` VARCHAR(100) NULL,
    `latitude` DECIMAL(9, 6) NULL,
    `longitude` DECIMAL(9, 6) NULL,
    `jsonData` TEXT NULL COMMENT 'Full JSON from Perplexity',
    `createdAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`tripAttractionId`),
    CONSTRAINT `fk_TripAttraction_Trip`
        FOREIGN KEY (`tbl_TriptripId`)
        REFERENCES `tbl_Trip` (`tripId`)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== CHAT WITH PERPLEXITY ====================

CREATE TABLE `tbl_ChatThread` (
    `threadId` INT(10) NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(255) NULL,
    `createdAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `lastMessageAt` TIMESTAMP NULL,
    `tbl_CustomercustomerId` INT(10) NOT NULL,
    PRIMARY KEY (`threadId`),
    CONSTRAINT `fk_ChatThread_Customer`
        FOREIGN KEY (`tbl_CustomercustomerId`)
        REFERENCES `tbl_Customer` (`customerId`)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tbl_ChatMessage` (
    `messageId` INT(10) NOT NULL AUTO_INCREMENT,
    `tbl_ChatThreadthreadId` INT(10) NOT NULL,
    `role` VARCHAR(20) NOT NULL COMMENT 'user, assistant, system',
    `content` TEXT NOT NULL,
    `createdAt` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`messageId`),
    CONSTRAINT `fk_ChatMessage_Thread`
        FOREIGN KEY (`tbl_ChatThreadthreadId`)
        REFERENCES `tbl_ChatThread` (`threadId`)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== NOTIFICATIONS ====================

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== INDEXES FOR PERFORMANCE ====================

CREATE INDEX idx_account_role ON tbl_Account(role);
CREATE INDEX idx_city_region ON tbl_City(tbl_RegionregionId);
CREATE INDEX idx_destination_city ON tbl_Destination(tbl_CitycityId);
CREATE INDEX idx_trip_customer ON tbl_Trip(tbl_CustomercustomerId);
CREATE INDEX idx_chatthread_customer ON tbl_ChatThread(tbl_CustomercustomerId);
CREATE INDEX idx_chatmessage_thread ON tbl_ChatMessage(tbl_ChatThreadthreadId);
CREATE INDEX idx_tripflight_trip ON tbl_TripFlight(tbl_TriptripId);
CREATE INDEX idx_triphotel_trip ON tbl_TripHotel(tbl_TriptripId);
CREATE INDEX idx_tripattraction_trip ON tbl_TripAttraction(tbl_TriptripId);

SET FOREIGN_KEY_CHECKS=1;

-- ==================== SAMPLE DATA ====================

-- Insert sample admin account
-- Password: Admin123@ (BCrypt hash)
INSERT INTO tbl_Customer (fullName, email, phone, address) 
VALUES ('Administrator', 'admin@tripapp.com', '0000000000', 'Admin Office');

INSERT INTO tbl_Account (userName, passwordHash, socialProvider, role, tbl_CustomercustomerId)
VALUES ('admin', '$2a$10$YES/fo7bb7JwqQJE/iIKwOYRBcDx.AQVdbVO68MYSYEB73KQnYX.C', 'LOCAL', 'ADMIN', 1);

-- Regions: Bắc, Trung, Nam
INSERT INTO tbl_Region (name, description)
VALUES
('Miền Bắc', 'Khu vực Bắc Bộ: Đông Bắc, Tây Bắc, Đồng bằng sông Hồng, gồm Hà Nội, Hà Giang, Lào Cai, Điện Biên, Ninh Bình...'),
('Miền Trung', 'Khu vực Trung Bộ: Bắc Trung Bộ, Duyên hải Nam Trung Bộ, Tây Nguyên, gồm Thừa Thiên Huế, Quảng Nam, Bình Định...'),
('Miền Nam', 'Khu vực Nam Bộ: Đông Nam Bộ, Tây Nam Bộ, gồm TP.HCM, Bà Rịa-Vũng Tàu (Côn Đảo)...'); 

SET @REGION_BAC = (SELECT regionId FROM tbl_Region WHERE name = 'Miền Bắc');
SET @REGION_TRUNG = (SELECT regionId FROM tbl_Region WHERE name = 'Miền Trung');
SET @REGION_NAM = (SELECT regionId FROM tbl_Region WHERE name = 'Miền Nam');

-- Hà Nội (Miền Bắc)
INSERT INTO tbl_City (name, tbl_RegionregionId, latitude, longitude, description)
VALUES
('Hà Nội', @REGION_BAC, 21.028511, 105.804817,
'Thủ đô nghìn năm văn hiến; khám phá Phố Cổ, Hoàng thành Thăng Long (UNESCO), Văn Miếu, Lăng Chủ tịch Hồ Chí Minh, Bảo tàng Dân tộc học, kiến trúc Pháp thuộc.');

-- Huế (Thừa Thiên Huế, Miền Trung)
INSERT INTO tbl_City (name, tbl_RegionregionId, latitude, longitude, description)
VALUES
('Huế', @REGION_TRUNG, 16.463713, 107.590866,
'Cố đô triều Nguyễn; Kinh thành Huế (Đại Nội), lăng Minh Mạng, Tự Đức, Khải Định, Chùa Thiên Mụ, Nhã nhạc cung đình Huế (UNESCO).');

-- Hội An (Quảng Nam, Miền Trung)
INSERT INTO tbl_City (name, tbl_RegionregionId, latitude, longitude, description)
VALUES
('Hội An', @REGION_TRUNG, 15.87972, 108.33194,
'Đô thị cổ Di sản UNESCO; dạo Phố cổ đêm đèn lồng, Chùa Cầu, hội quán Phúc Kiến/Quảng Đông, nhà cổ Tấn Ký, Phùng Hưng.');

-- TP. Hồ Chí Minh (Miền Nam)
INSERT INTO tbl_City (name, tbl_RegionregionId, latitude, longitude, description)
VALUES
('TP. Hồ Chí Minh', @REGION_NAM, 10.762622, 106.660172,
'Trung tâm kinh tế sôi động; Dinh Độc Lập, Bảo tàng Chứng tích Chiến tranh, Địa đạo Củ Chi, Bưu điện Trung tâm, Nhà thờ Đức Bà.');

-- Ninh Bình (Miền Bắc)
INSERT INTO tbl_City (name, tbl_RegionregionId, latitude, longitude, description)
VALUES
('Ninh Bình', @REGION_BAC, 20.2506149, 105.9744536,
'Kinh đô Hoa Lư thời Đinh - Tiền Lê; Cố đô Hoa Lư, quần thể Tràng An (UNESCO), chùa Bái Đính.');

-- Sapa (Lào Cai, Miền Bắc)
INSERT INTO tbl_City (name, tbl_RegionregionId, latitude, longitude, description)
VALUES
('Sapa', @REGION_BAC, 22.33769, 103.84037,
'Văn hóa dân tộc thiểu số Tây Bắc (H Mông, Dao Đỏ, Tày, Giáy); bản Cát Cát, Tả Van, Tả Phìn; nghề dệt; Nhà thờ Đá thời Pháp.');

-- Điện Biên Phủ (Điện Biên, Miền Bắc)
INSERT INTO tbl_City (name, tbl_RegionregionId, latitude, longitude, description)
VALUES
('Điện Biên Phủ', @REGION_BAC, 21.38602, 103.02301,
'Chiến thắng 1954 lịch sử; thăm Đồi A1, Hầm Đờ Cát, Bảo tàng Chiến thắng Điện Biên Phủ, Nghĩa trang Liệt sĩ.');

-- Quy Nhơn (Bình Định, Miền Trung)
INSERT INTO tbl_City (name, tbl_RegionregionId, latitude, longitude, description)
VALUES
('Quy Nhơn', @REGION_TRUNG, 13.770409, 109.232667,
'Trung tâm văn hóa Chăm-pa; tháp Chăm (Tháp Đôi, Bánh Ít); Bảo tàng Quang Trung và võ cổ truyền Bình Định.');

-- Hà Giang (Miền Bắc)
INSERT INTO tbl_City (name, tbl_RegionregionId, latitude, longitude, description)
VALUES
('Hà Giang', @REGION_BAC, 22.8025588, 104.9784494,
'Nhiều dân tộc thiểu số; Công viên địa chất Toàn cầu UNESCO Cao nguyên đá Đồng Văn; Phố cổ Đồng Văn, Dinh họ Vương, Cột cờ Lũng Cú, chợ vùng cao.');

-- Côn Đảo (Bà Rịa–Vũng Tàu, Miền Nam)
INSERT INTO tbl_City (name, tbl_RegionregionId, latitude, longitude, description)
VALUES
('Côn Đảo', @REGION_NAM, 8.6953, 106.6190,
'Thiên nhiên đẹp và di tích lịch sử “địa ngục trần gian”: hệ thống nhà tù (Chuồng cọp, Chuồng bò), Bảo tàng Côn Đảo, Nghĩa trang Hàng Dương.');

