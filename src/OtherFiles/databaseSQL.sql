-- Drink Enterprise Database Schema DDL for MySQL
 CREATE DATABASE IF NOT EXISTS drink_enterprise_db;
 USE drink_enterprise_db;

-- 1. Drinks Table
CREATE TABLE IF NOT EXISTS drinks (
    drink_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    brand VARCHAR(100),
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0)
);

-- 2. Branches Table
CREATE TABLE IF NOT EXISTS branches (
    branch_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255)
);

-- 3. Stock Items Table (Manages stock per drink per branch)
CREATE TABLE IF NOT EXISTS stock_items (
    branch_id VARCHAR(50) NOT NULL,
    drink_id VARCHAR(50) NOT NULL,
    quantity INT NOT NULL CHECK (quantity >= 0),
    minimum_threshold INT DEFAULT 0 CHECK (minimum_threshold >= 0),
    PRIMARY KEY (branch_id, drink_id),
    FOREIGN KEY (branch_id) REFERENCES branches(branch_id) ON DELETE CASCADE,
    FOREIGN KEY (drink_id) REFERENCES drinks(drink_id) ON DELETE CASCADE
);

-- 4. Users Table (For system authentication/authorization)
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(100) PRIMARY KEY,
    hashed_password VARCHAR(255) NOT NULL, -- Store securely hashed passwords!
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'BRANCH_MANAGER', 'STAFF')) -- Example roles
);

-- 5. Orders Table
CREATE TABLE IF NOT EXISTS orders (
    order_id VARCHAR(100) PRIMARY KEY,
    customer_id VARCHAR(100) NOT NULL, -- Can be a generic ID for guest customers
    branch_id VARCHAR(50) NOT NULL,
    order_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(12, 2) NOT NULL CHECK (total_amount >= 0),
    FOREIGN KEY (branch_id) REFERENCES branches(branch_id) ON DELETE RESTRICT -- Prevent deleting branch if orders exist
);

-- 6. Order Items Table (Details of each item in an order)
CREATE TABLE IF NOT EXISTS order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL,
    drink_id VARCHAR(50) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    price_at_time_of_order DECIMAL(10, 2) NOT NULL CHECK (price_at_time_of_order >= 0),
    item_total DECIMAL(12, 2) NOT NULL CHECK (item_total >= 0), -- quantity * price_at_time_of_order
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (drink_id) REFERENCES drinks(drink_id) ON DELETE RESTRICT -- Prevent deleting drink if it's in an order
);

/*
-- --- Sample Data (Optional - for initial setup and testing) ---

-- Add HQ Branch first as other stock might depend on it
INSERT IGNORE INTO branches (branch_id, name, location) VALUES
('NAIROBI_HQ', 'Nairobi Headquarters', 'Nairobi');

-- Add some drinks
INSERT IGNORE INTO drinks (drink_id, name, brand, price) VALUES
('DK001', 'Coca-Cola 500ml', 'Coca-Cola', 60.00),
('DK002', 'Pepsi 500ml', 'PepsiCo', 55.00),
('DK003', 'Fanta Orange 300ml', 'Coca-Cola', 45.00),
('DK004', 'Sprite 300ml', 'Coca-Cola', 45.00),
('DK005', 'Minute Maid Orange Juice 400ml', 'Coca-Cola', 70.00);

-- Add initial stock for HQ
INSERT IGNORE INTO stock_items (branch_id, drink_id, quantity, minimum_threshold) VALUES
('NAIROBI_HQ', 'DK001', 1000, 50),
('NAIROBI_HQ', 'DK002', 800, 40),
('NAIROBI_HQ', 'DK003', 700, 30);

-- Add other branches
INSERT IGNORE INTO branches (branch_id, name, location) VALUES
('NKR01', 'Nakuru', 'Nakuru Town'),
('MSA01', 'Mombasa', 'Mombasa City'),
('KSM01', 'Kisumu', 'Kisumu City');

-- Add some stock to other branches (simulating initial transfer from HQ)
INSERT IGNORE INTO stock_items (branch_id, drink_id, quantity, minimum_threshold) VALUES
('NKR01', 'DK001', 100, 20),
('NKR01', 'DK002', 80, 15),
('MSA01', 'DK001', 150, 25);


-- Add a default admin user (Password: "admin_pass")
INSERT IGNORE INTO users (username, hashed_password, role) VALUES
('admin', '$2a$12$LaaitztNyfmmHwCtd0jkDu6gMgQoZHtV4XI64s4cGKaemi1IUstNS', 'ADMIN'), -- plaintextpassword= 'admin_pass'
('nakuru_mgr', '$2a$12$zRqOI5H7yd9pubP/2CNmcuX38szbUDpFdkUFvki.szoQLm7XuIZVK', 'BRANCH_MANAGER'); -- plaintextpassword='nakupass'

*/