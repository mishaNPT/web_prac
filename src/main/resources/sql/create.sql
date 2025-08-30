-- Удаление таблиц если существуют (для пересоздания)
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS flights CASCADE;
DROP TABLE IF EXISTS clients CASCADE;
DROP TABLE IF EXISTS airlines CASCADE;

-- Создание таблицы авиакомпаний
CREATE TABLE airlines (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    miles_rate DECIMAL(3,2) DEFAULT 1.0
);

-- Создание таблицы клиентов
CREATE TABLE clients (
    id SERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    address TEXT,
    bonus_miles INTEGER DEFAULT 0
);

-- Создание таблицы рейсов
CREATE TABLE flights (
    id SERIAL PRIMARY KEY,
    flight_number VARCHAR(10) NOT NULL,
    airline_id INTEGER REFERENCES airlines(id),
    departure_airport VARCHAR(10) NOT NULL,
    arrival_airport VARCHAR(10) NOT NULL,
    departure_time TIMESTAMP NOT NULL,
    arrival_time TIMESTAMP NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    total_seats INTEGER NOT NULL,
    available_seats INTEGER NOT NULL
);

-- Создание таблицы бронирований
CREATE TABLE bookings (
    id SERIAL PRIMARY KEY,
    client_id INTEGER REFERENCES clients(id),
    flight_id INTEGER REFERENCES flights(id),
    booking_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'BOOKED',
    paid_with_miles BOOLEAN DEFAULT FALSE,
    miles_used INTEGER DEFAULT 0
);
