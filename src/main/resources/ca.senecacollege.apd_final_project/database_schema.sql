-- Hotel Reservation System Database Schema

-- Create database
CREATE DATABASE IF NOT EXISTS hotel_reservation;
USE hotel_reservation;

-- Create guests table
CREATE TABLE IF NOT EXISTS guests (
    guest_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    feedback TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create room types enum
CREATE TABLE IF NOT EXISTS room_types (
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

-- Create rooms table
CREATE TABLE IF NOT EXISTS rooms (
    room_id INT AUTO_INCREMENT PRIMARY KEY,
    room_type_id TINYINT NOT NULL,
    room_number VARCHAR(10) NOT NULL UNIQUE,
    floor INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (room_type_id) REFERENCES room_types(room_type_id)
);

-- Insert some sample rooms
INSERT INTO rooms (room_type_id, room_number, floor, price) VALUES
(1, '101', 1, 100.00),
(1, '102', 1, 100.00),
(1, '103', 1, 100.00),
(1, '104', 1, 100.00),
(2, '201', 2, 180.00),
(2, '202', 2, 180.00),
(2, '203', 2, 180.00),
(3, '301', 3, 250.00),
(3, '302', 3, 250.00),
(4, '401', 4, 400.00);

-- Create reservations table
CREATE TABLE IF NOT EXISTS reservations (
    reservation_id INT AUTO_INCREMENT PRIMARY KEY,
    guest_id INT NOT NULL,
    room_id INT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    number_of_guests INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (guest_id) REFERENCES guests(guest_id),
    FOREIGN KEY (room_id) REFERENCES rooms(room_id)
);

-- Create bills table
CREATE TABLE IF NOT EXISTS bills (
    bill_id INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    tax DECIMAL(10, 2) NOT NULL,
    discount DECIMAL(10, 2) DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL,
    billing_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_paid BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id)
);

-- Create feedback table
CREATE TABLE IF NOT EXISTS feedback (
    feedback_id INT AUTO_INCREMENT PRIMARY KEY,
    guest_id INT NOT NULL,
    reservation_id INT NOT NULL,
    rating INT NOT NULL,
    comments TEXT,
    submission_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (guest_id) REFERENCES guests(guest_id),
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id)
);

-- Create admins table
CREATE TABLE IF NOT EXISTS admins (
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

-- Create kiosks table
CREATE TABLE IF NOT EXISTS kiosks (
    kiosk_id INT AUTO_INCREMENT PRIMARY KEY,
    location VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    last_activity TIMESTAMP NULL
);

-- Insert kiosk locations
INSERT INTO kiosks (location, status) VALUES
('Main Lobby', 'ACTIVE'),
('East Entrance', 'ACTIVE');

-- Create indexes for frequently queried columns
CREATE INDEX idx_reservations_status ON reservations(status);
CREATE INDEX idx_reservations_dates ON reservations(check_in_date, check_out_date);
CREATE INDEX idx_guests_name ON guests(name);
CREATE INDEX idx_guests_phone ON guests(phone_number);
CREATE INDEX idx_guests_email ON guests(email);

-- Create a view for active reservations
CREATE VIEW view_active_reservations AS
SELECT r.reservation_id, g.name AS guest_name, g.phone_number,
       rm.room_number, rt.name AS room_type,
       r.check_in_date, r.check_out_date, r.number_of_guests, r.status
FROM reservations r
JOIN guests g ON r.guest_id = g.guest_id
JOIN rooms rm ON r.room_id = rm.room_id
JOIN room_types rt ON rm.room_type_id = rt.room_type_id
WHERE r.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN');

-- Create a view for today's check-ins
CREATE VIEW view_today_check_ins AS
SELECT r.reservation_id, g.name AS guest_name, g.phone_number,
       rm.room_number, rt.name AS room_type, r.number_of_guests
FROM reservations r
JOIN guests g ON r.guest_id = g.guest_id
JOIN rooms rm ON r.room_id = rm.room_id
JOIN room_types rt ON rm.room_type_id = rt.room_type_id
WHERE r.check_in_date = CURDATE() AND r.status = 'CONFIRMED';

-- Create a view for today's check-outs
CREATE VIEW view_today_check_outs AS
SELECT r.reservation_id, g.name AS guest_name, g.phone_number,
       rm.room_number, rt.name AS room_type
FROM reservations r
JOIN guests g ON r.guest_id = g.guest_id
JOIN rooms rm ON r.room_id = rm.room_id
JOIN room_types rt ON rm.room_type_id = rt.room_type_id
WHERE r.check_out_date = CURDATE() AND r.status = 'CHECKED_IN';

-- Create a view for room availability
CREATE VIEW view_room_availability AS
SELECT rm.room_id, rm.room_number, rt.name AS room_type,
       rm.price, rm.is_available
FROM rooms rm
JOIN room_types rt ON rm.room_type_id = rt.room_type_id
WHERE rm.is_available = TRUE;