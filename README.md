# booking-service

#  Booking Service

Cleaning professional booking system built with Spring Boot 3.5, MySQL.

## Prerequisites

- Java 17+
- Maven
- Docker & Docker Compose

## Run

```bash
# 1. Start MySQL
docker compose up -d

# 2. Start the application (Flyway handles schema + seed data)
./mvnw spring-boot:run
```

## API

Base URL: `http://localhost:8080`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/availability?date=2026-03-10` | Check availability for a day |
| GET | `/api/v1/availability?date=2026-03-10&startTime=08:00&duration=2` | Check availability for a specific slot |
| POST | `/api/v1/bookings` | Create a booking |
| PUT | `/api/v1/bookings/{id}` | Update a booking |
| GET | `/api/v1/bookings/{id}` | Get booking details |

### Create Booking

```bash
curl -X POST http://localhost:8080/api/v1/bookings \
  -H 'Content-Type: application/json' \
  -d '{
    "date": "2026-03-10",
    "startTime": "08:00",
    "duration": 2,
    "professionalCount": 1
  }'
```

### Update Booking

```bash
curl -X PUT http://localhost:8080/api/v1/bookings/1 \
  -H 'Content-Type: application/json' \
  -d '{
    "date": "2026-03-11",
    "startTime": "10:00"
  }'
```

### Get Booking

```bash
curl http://localhost:8080/api/v1/bookings/1
```

### Swagger UI

http://localhost:8080/swagger-ui.html

## Business Rules

- Working hours: 08:00 - 22:00
- Booking duration: 2 or 4 hours
- Professionals per booking: 1, 2, or 3 (must be from the same vehicle)
- 30-minute mandatory break between appointments
- No bookings on Fridays

