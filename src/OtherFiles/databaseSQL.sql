CREATE DATABASE IF NOT EXISTS drink_enterprise_db;
USE drink_enterprise_db;

CREATE TABLE IF NOT EXISTS branches (
    branch_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255)
);

-- 2. Drinks Table
CREATE TABLE IF NOT EXISTS drinks (
    drink_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    brand VARCHAR(100),
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0)
);

-- 3. Users Table (Updated with branch_id)
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(100) PRIMARY KEY,
    hashed_password VARCHAR(255) NOT NULL, -- Store securely hashed passwords!
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'BRANCH_MANAGER', 'STAFF')),
    branch_id VARCHAR(50) NULL DEFAULT NULL, -- Link to a branch, NULL for ADMIN
    CONSTRAINT fk_user_branch FOREIGN KEY (branch_id) REFERENCES branches(branch_id)
);

-- 4. Stock Items Table
CREATE TABLE IF NOT EXISTS stock_items (
    branch_id VARCHAR(50) NOT NULL,
    drink_id VARCHAR(50) NOT NULL,
    quantity INT NOT NULL CHECK (quantity >= 0),
    minimum_threshold INT DEFAULT 0 CHECK (minimum_threshold >= 0),
    PRIMARY KEY (branch_id, drink_id),
    FOREIGN KEY (branch_id) REFERENCES branches(branch_id) ON DELETE CASCADE,
    FOREIGN KEY (drink_id) REFERENCES drinks(drink_id) ON DELETE CASCADE
);

-- 5. Orders Table
CREATE TABLE IF NOT EXISTS orders (
    order_id VARCHAR(100) PRIMARY KEY,
    customer_id VARCHAR(100) NOT NULL,
    branch_id VARCHAR(50) NOT NULL,
    order_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(12, 2) NOT NULL CHECK (total_amount >= 0),
    FOREIGN KEY (branch_id) REFERENCES branches(branch_id) ON DELETE RESTRICT
);

-- 6. Order Items Table
CREATE TABLE IF NOT EXISTS order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(100) NOT NULL,
    drink_id VARCHAR(50) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    price_at_time_of_order DECIMAL(10, 2) NOT NULL CHECK (price_at_time_of_order >= 0),
    item_total DECIMAL(12, 2) NOT NULL CHECK (item_total >= 0),
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (drink_id) REFERENCES drinks(drink_id) ON DELETE RESTRICT
);