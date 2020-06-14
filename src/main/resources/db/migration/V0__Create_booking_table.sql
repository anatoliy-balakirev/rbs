CREATE TABLE booking (
    id UUID PRIMARY KEY,
    client_id UUID NOT NULL,
    amount DECIMAL NOT NULL,
    currency VARCHAR(10) NOT NULL,
    creation_time TIMESTAMP NOT NULL DEFAULT current_timestamp,
    description VARCHAR(1000)
);
CREATE INDEX idx_booking_client_id ON booking(client_id);