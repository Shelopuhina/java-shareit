DROP TABLE IF EXISTS items CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
CREATE TABLE users (
    user_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_name VARCHAR NOT NULL,
    user_email VARCHAR UNIQUE NOT NULL
    );

CREATE TABLE item_requests (
    request_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    request_description VARCHAR NOT NULL,
    requestor_id INTEGER NOT NULL REFERENCES users (user_id),
    request_created TIMESTAMP NOT NULL
    );

CREATE TABLE items
(
    item_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    item_name VARCHAR NOT NULL,
    item_description VARCHAR NOT NULL,
    item_available BOOLEAN NOT NULL,
    item_id_owner INTEGER REFERENCES users (user_id),
    request_id INTEGER REFERENCES item_requests (request_id)
);

CREATE TABLE bookings
(
    booking_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    booking_start_time TIMESTAMP NOT NULL,
    booking_end_time TIMESTAMP NOT NULL,
    item_id INTEGER NOT NULL REFERENCES items (item_id),
    status_booking ENUM('APPROVED', 'CANCELED', 'REJECTED', 'WAITING'),
    id_user_booker INTEGER NOT NULL REFERENCES users (user_id)
);

CREATE TABLE comments
(
    comment_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    comment_text VARCHAR   NOT NULL,
    item_id_comment INTEGER NOT NULL REFERENCES items (item_id),
    author_id_comment INTEGER NOT NULL REFERENCES users (user_id),
    comment_date_creation TIMESTAMP NOT NULL
);
