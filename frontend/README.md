# RideSync Frontend

React + Tailwind CSS frontend for the RideSync carpooling platform.

## Tech Stack
- React 18, Vite
- React Router v6
- Tailwind CSS
- Axios (with JWT interceptors)
- lucide-react (icons)
- date-fns (date formatting)

## Setup

### Prerequisites
- Node.js 18+
- Backend running on `http://localhost:8080`

### Install & Run
```bash
npm install
npm run dev
```
Opens at `http://localhost:5173`. API calls are proxied to port 8080.

## Pages

| Route | Role | Description |
|-------|------|-------------|
| `/` | Public | Landing page |
| `/register` | Public | Sign up (RIDER or DRIVER) |
| `/login` | Public | Sign in |
| `/dashboard` | Any | Role-based dashboard with stats |
| `/rides/search` | Public | Search + book rides |
| `/rides/create` | DRIVER | Post a new ride |
| `/my-rides` | DRIVER | Manage rides, view bookings |
| `/my-bookings` | RIDER | View + cancel bookings |
| `/profile` | Any | Edit name/phone |
| `/admin` | ADMIN | User management, ride oversight, audit log |

## Features
- JWT stored in localStorage, attached to all requests
- 401 responses → auto logout + redirect to /login
- Role-based route protection (ProtectedRoute component)
- Banned users see error on login attempt
- Admin panel: ban/unban users, change roles, cancel rides, view audit log
