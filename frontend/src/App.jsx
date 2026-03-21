import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import Navbar from './components/Navbar'
import ProtectedRoute from './components/ProtectedRoute'

import Home from './pages/Home'
import Login from './pages/Login'
import Register from './pages/Register'
import Dashboard from './pages/Dashboard'
import SearchRides from './pages/SearchRides'
import CreateRide from './pages/CreateRide'
import MyRides from './pages/MyRides'
import MyBookings from './pages/MyBookings'
import Profile from './pages/Profile'
import AdminPanel from './pages/AdminPanel'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="min-h-screen flex flex-col">
          <Navbar />
          <main className="flex-1">
            <Routes>
              <Route path="/" element={<Home />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route path="/rides/search" element={<SearchRides />} />

              <Route path="/dashboard" element={
                <ProtectedRoute><Dashboard /></ProtectedRoute>
              } />
              <Route path="/profile" element={
                <ProtectedRoute><Profile /></ProtectedRoute>
              } />
              <Route path="/rides/create" element={
                <ProtectedRoute roles={['DRIVER']}><CreateRide /></ProtectedRoute>
              } />
              <Route path="/my-rides" element={
                <ProtectedRoute roles={['DRIVER']}><MyRides /></ProtectedRoute>
              } />
              <Route path="/my-bookings" element={
                <ProtectedRoute roles={['RIDER']}><MyBookings /></ProtectedRoute>
              } />
              <Route path="/admin" element={
                <ProtectedRoute roles={['ADMIN']}><AdminPanel /></ProtectedRoute>
              } />

              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </main>
          <footer className="border-t border-slate-200 bg-white py-4 text-center text-xs text-slate-400">
            © {new Date().getFullYear()} RideSync · College Carpool Platform
          </footer>
        </div>
      </BrowserRouter>
    </AuthProvider>
  )
}
