CREATE TABLE vehicles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    plate_number VARCHAR(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE professionals (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    vehicle_id BIGINT       NOT NULL,
    version    BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT fk_professional_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE bookings (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    date       DATE         NOT NULL,
    start_time TIME         NOT NULL,
    end_time   TIME         NOT NULL,
    duration   INT          NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'CONFIRMED',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE booking_professionals (
    booking_id      BIGINT NOT NULL,
    professional_id BIGINT NOT NULL,
    PRIMARY KEY (booking_id, professional_id),
    CONSTRAINT fk_bp_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_bp_professional FOREIGN KEY (professional_id) REFERENCES professionals(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_booking_date ON bookings(date);
CREATE INDEX idx_professional_vehicle ON professionals(vehicle_id);
CREATE INDEX idx_bp_professional ON booking_professionals(professional_id);
