# RideSync Backend

Spring Boot REST API for the RideSync carpooling platform.

## Tech Stack
- Java 17, Spring Boot 3.2.1
- Spring Security + JWT (stateless authentication)
- Spring Data JPA + MySQL
- BCrypt password hashing
- Maven

## Setup

### Prerequisites
- Java 17+
- MySQL 8+ running on `localhost:3306`
- Maven 3.8+

### Configuration
Edit `src/main/resources/application.yml`:
```yaml
spring.datasource.username: your_mysql_user
spring.datasource.password: your_mysql_password
app.jwt.secret: <your-256-bit-secret>
```

### Run
```bash
mvn spring-boot:run
```
Server starts on `http://localhost:8080`. The database `ridesync_db` is created automatically.

## API Overview

### Auth (public)
| Method | Endpoint | Body |
|--------|----------|------|
| POST | `/api/users/register` | `{name, email, password, phone, role}` |
| POST | `/api/users/login` | `{email, password}` |

Both return `{token, userId, name, email, phone, role, banned}`.  
Add the token as `Authorization: Bearer <token>` on all protected endpoints.

### Rides
| Method | Endpoint | Auth |
|--------|----------|------|
| GET | `/api/rides/search?source=X&destination=Y` | Public |
| GET | `/api/rides/{id}` | Public |
| POST | `/api/rides` | DRIVER |
| GET | `/api/rides/my-rides` | DRIVER |
| PUT | `/api/rides/{id}/status?status=IN_PROGRESS` | DRIVER/ADMIN |

### Bookings
| Method | Endpoint | Auth |
|--------|----------|------|
| POST | `/api/bookings?rideId=X&seats=Y` | RIDER |
| PUT | `/api/bookings/{id}/cancel` | Authenticated |
| GET | `/api/bookings/my-bookings` | RIDER |
| GET | `/api/bookings/ride/{rideId}` | Authenticated |

### Admin
All `/api/admin/**` endpoints require `ADMIN` role.

| Method | Endpoint |
|--------|----------|
| GET | `/api/admin/users` |
| PUT | `/api/admin/users/{id}/ban` |
| PUT | `/api/admin/users/{id}/unban` |
| PUT | `/api/admin/users/{id}/role?role=DRIVER` |
| GET | `/api/admin/rides` |
| PUT | `/api/admin/rides/{id}/cancel` |
| GET | `/api/admin/audit` |

## Security improvements over original
- Full JWT authentication (stateless, no sessions)
- Role-based access control via Spring Security
- Proper `banned` boolean field (not phone field hack)
- Banned users blocked at login AND request time
- CORS locked to localhost:5173 / localhost:3000
- Drivers can't book their own rides
- `@PreAuthorize` guards on all sensitive endpoints
