CREATE TABLE product (
    id IDENTITY PRIMARY KEY,
    sku VARCHAR(256),
    name VARCHAR(256),
    price DECIMAL(20, 2)
);

CREATE TABLE customer (
    id IDENTITY PRIMARY KEY,
    name VARCHAR(256),
    birthdate DATE
);

CREATE TABLE address (
    id INT8 PRIMARY KEY,
    street VARCHAR(128),
    postal_code VARCHAR(16)
);

CREATE TABLE email_address (
    id IDENTITY PRIMARY KEY,
    customer_id INT8,
    address VARCHAR(128)
);

CREATE TABLE purchase_order (
    id IDENTITY PRIMARY KEY,
    customer_id INT8,
    order_date DATE
);

CREATE TABLE item (
    id IDENTITY PRIMARY KEY,
    order_id INT8,
    product_id INT8,
    quantity INT,
    total DECIMAL(20, 2)
);