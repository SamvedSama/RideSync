# RideSync – College Carpool Platform

A full-stack carpooling application with Spring Boot backend and React frontend.

## Project Structure
```
ridesync_final/
├── backend/    ← Spring Boot 3.2 + JWT + MySQL
└── frontend/   ← React 18 + Vite + Tailwind CSS
```

## Quick Start

### Prerequisites
- Java 17+, Maven 3.8+
- MySQL 8+ on localhost:3306
- Node.js 18+

### 1. Start the Backend
```bash
cd backend
# Edit src/main/resources/application.yml with your MySQL credentials
mvn spring-boot:run
# → Starts on http://localhost:8080
# → Creates ridesync_db automatically
```

### 2. Start the Frontend
```bash
cd frontend
npm install
npm run dev
# → Opens at http://localhost:5173
```

### 3. Create your first Admin
Register via the API directly (or via the UI, then promote via DB):
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Admin","email":"admin@ridesync.com","password":"admin123","phone":"0000000000","role":"ADMIN"}'
```

## Running Tests
```bash
cd backend
mvn test
# Tests run on H2 in-memory DB — no MySQL needed
```

See `backend/README.md` and `frontend/README.md` for full API and feature docs.
