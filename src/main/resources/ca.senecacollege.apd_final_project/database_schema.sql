CREATE DATABASE hotel_reservation;
USE hotel_reservation;

-- Create user with appropriate permissions
CREATE USER IF NOT EXISTS 'hotel_admin'@'localhost' IDENTIFIED BY 'SecureHotel2025!';
GRANT ALL PRIVILEGES ON hotel_reservation.* TO 'hotel_admin'@'localhost';
FLUSH PRIVILEGES;

-- Create tables

-- Room types enum table
CREATE TABLE room_types (
    room_type_id TINYINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    base_price DECIMAL(10, 2) NOT NULL,
    max_occupancy INT NOT NULL
);

-- Insert room types
INSERT INTO room_types (room_type_id, name, base_price, max_occupancy) VALUES
(1, 'SINGLE', 100.00, 2),
(2, 'DOUBLE', 180.00, 4),
(3, 'DELUXE', 250.00, 2),
(4, 'PENT_HOUSE', 400.00, 2);

-- Guest Table - Email as unique identifier
CREATE TABLE guests (
    guest_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    address VARCHAR(255) NOT NULL,
    feedback TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Room Table
CREATE TABLE rooms (
    room_id INT AUTO_INCREMENT PRIMARY KEY,
    room_type_id TINYINT NOT NULL,
    room_number VARCHAR(10) NOT NULL UNIQUE,
    floor INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (room_type_id) REFERENCES room_types(room_type_id)
);

-- Reservation Table
CREATE TABLE reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    guest_id INT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    number_of_guests INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (guest_id) REFERENCES guests(guest_id),
    CONSTRAINT check_dates CHECK (check_out_date > check_in_date)
);

-- Junction table for multiple rooms per reservation
CREATE TABLE reservation_rooms (
    reservation_room_id INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT NOT NULL,
    room_id INT NOT NULL,
    price_per_night DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(room_id)
);

-- Bills table
CREATE TABLE bills (
    bill_id INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    tax DECIMAL(10, 2) NOT NULL,
    discount DECIMAL(10, 2) DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(20),
    billing_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_paid BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id)
);

-- Admin Table
CREATE TABLE admins (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
);

-- Insert default admin accounts
INSERT INTO admins (username, password, name, role) VALUES
('admin', 'admin123', 'System Administrator', 'ADMIN'),
('manager', 'manager123', 'Hotel Manager', 'MANAGER');

-- Feedback Table
CREATE TABLE feedback (
    feedback_id INT AUTO_INCREMENT PRIMARY KEY,
    guest_id INT NOT NULL,
    reservation_id INT NOT NULL,
    rating INT NOT NULL,
    comments TEXT,
    submission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (guest_id) REFERENCES guests(guest_id),
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id),
    CONSTRAINT check_rating CHECK (rating BETWEEN 1 AND 5)
);

-- Create indexes for performance
CREATE INDEX idx_reservations_status ON reservations(status);
CREATE INDEX idx_reservations_dates ON reservations(check_in_date, check_out_date);
CREATE INDEX idx_guests_name ON guests(name);
CREATE INDEX idx_guests_phone ON guests(phone_number);
CREATE INDEX idx_guests_email ON guests(email);

-- Create a view for active reservations
CREATE VIEW view_active_reservations AS
SELECT r.reservation_id, g.name AS guest_name, g.phone_number,
       r.check_in_date, r.check_out_date, r.number_of_guests, r.status
FROM reservations r
JOIN guests g ON r.guest_id = g.guest_id
WHERE r.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN');

-- Create a view for today's check-ins
CREATE VIEW view_today_check_ins AS
SELECT r.reservation_id, g.name AS guest_name, g.phone_number,
       r.number_of_guests
FROM reservations r
JOIN guests g ON r.guest_id = g.guest_id
WHERE r.check_in_date = CURDATE() AND r.status = 'CONFIRMED';

-- Create a view for today's check-outs
CREATE VIEW view_today_check_outs AS
SELECT r.reservation_id, g.name AS guest_name, g.phone_number
FROM reservations r
JOIN guests g ON r.guest_id = g.guest_id
WHERE r.check_out_date = CURDATE() AND r.status = 'CHECKED_IN';

-- Create a view for room availability
CREATE VIEW view_room_availability AS
SELECT rm.room_id, rm.room_number, rt.name AS room_type,
       rm.price, rm.is_available
FROM rooms rm
JOIN room_types rt ON rm.room_type_id = rt.room_type_id
WHERE rm.is_available = TRUE;

-- Insert sample rooms
-- Single Rooms (40 rooms)
INSERT INTO rooms (room_type_id, room_number, floor, price)
SELECT
    1 AS room_type_id,
    CONCAT('1', LPAD(seq.n, 2, '0')) AS room_number,
    1 AS floor,
    100.00 AS price
FROM
    (SELECT n
     FROM
         (SELECT  1 AS n UNION SELECT  2 UNION SELECT  3 UNION SELECT  4 UNION SELECT  5 UNION
                 SELECT  6 UNION SELECT  7 UNION SELECT  8 UNION SELECT  9 UNION SELECT 10 UNION
                 SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION
                 SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20 UNION
                 SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25 UNION
                 SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30 UNION
                 SELECT 31 UNION SELECT 32 UNION SELECT 33 UNION SELECT 34 UNION SELECT 35 UNION
                 SELECT 36 UNION SELECT 37 UNION SELECT 38 UNION SELECT 39 UNION SELECT 40) nums) seq;

-- Double Rooms (30 rooms)
INSERT INTO rooms (room_type_id, room_number, floor, price)
SELECT
    2 AS room_type_id,
    CONCAT('2', LPAD(seq.n, 2, '0')) AS room_number,
    2 AS floor,
    180.00 AS price
FROM
    (SELECT n
     FROM
         (SELECT  1 AS n UNION SELECT  2 UNION SELECT  3 UNION SELECT  4 UNION SELECT  5 UNION
                 SELECT  6 UNION SELECT  7 UNION SELECT  8 UNION SELECT  9 UNION SELECT 10 UNION
                 SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION
                 SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20 UNION
                 SELECT 21 UNION SELECT 22 UNION SELECT 23 UNION SELECT 24 UNION SELECT 25 UNION
                 SELECT 26 UNION SELECT 27 UNION SELECT 28 UNION SELECT 29 UNION SELECT 30) nums) seq;

-- Deluxe Rooms (20 rooms)
INSERT INTO rooms (room_type_id, room_number, floor, price)
SELECT
    3 AS room_type_id,
    CONCAT('3', LPAD(seq.n, 2, '0')) AS room_number,
    3 AS floor,
    250.00 AS price
FROM
    (SELECT n
     FROM
         (SELECT  1 AS n UNION SELECT  2 UNION SELECT  3 UNION SELECT  4 UNION SELECT  5 UNION
                 SELECT  6 UNION SELECT  7 UNION SELECT  8 UNION SELECT  9 UNION SELECT 10 UNION
                 SELECT 11 UNION SELECT 12 UNION SELECT 13 UNION SELECT 14 UNION SELECT 15 UNION
                 SELECT 16 UNION SELECT 17 UNION SELECT 18 UNION SELECT 19 UNION SELECT 20) nums) seq;

-- Pent House Rooms (10 rooms)
INSERT INTO rooms (room_type_id, room_number, floor, price)
SELECT
    4 AS room_type_id,
    CONCAT('4', LPAD(seq.n, 2, '0')) AS room_number,
    4 AS floor,
    400.00 AS price
FROM
    (SELECT n
     FROM
         (SELECT 1 AS n UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION
                 SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) nums) seq;