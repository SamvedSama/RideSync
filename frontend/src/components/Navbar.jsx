import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Car, LogOut, User, LayoutDashboard, Search, PlusCircle, BookOpen, Shield } from 'lucide-react'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const isActive = (path) => location.pathname === path

  const navLink = (to, label, Icon) => (
    <Link
      to={to}
      className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${
        isActive(to)
          ? 'bg-brand-100 text-brand-700'
          : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
      }`}
    >
      <Icon size={15} />
      {label}
    </Link>
  )

  return (
    <nav className="sticky top-0 z-50 bg-white border-b border-slate-200 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex h-14 items-center justify-between">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2 font-bold text-brand-600 text-lg">
            <Car size={22} className="text-brand-600" />
            RideSync
          </Link>

          {/* Nav links */}
          {user && (
            <div className="flex items-center gap-1">
              {navLink('/dashboard', 'Dashboard', LayoutDashboard)}
              {navLink('/rides/search', 'Find Rides', Search)}
              {user.role === 'DRIVER' && navLink('/rides/create', 'Post Ride', PlusCircle)}
              {user.role === 'DRIVER' && navLink('/my-rides', 'My Rides', Car)}
              {user.role === 'RIDER' && navLink('/my-bookings', 'My Bookings', BookOpen)}
              {user.role === 'ADMIN' && navLink('/admin', 'Admin', Shield)}
            </div>
          )}

          {/* User area */}
          <div className="flex items-center gap-2">
            {user ? (
              <>
                <Link to="/profile" className="flex items-center gap-2 px-3 py-1.5 rounded-lg hover:bg-slate-100 transition-colors">
                  <div className="w-7 h-7 rounded-full bg-brand-600 flex items-center justify-center text-white text-xs font-bold">
                    {user.name?.charAt(0).toUpperCase()}
                  </div>
                  <span className="text-sm font-medium text-slate-700 hidden sm:block">{user.name}</span>
                </Link>
                <button
                  onClick={handleLogout}
                  className="flex items-center gap-1 px-3 py-1.5 text-sm text-slate-600 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                >
                  <LogOut size={15} />
                  <span className="hidden sm:block">Logout</span>
                </button>
              </>
            ) : (
              <div className="flex gap-2">
                <Link to="/login" className="btn-secondary text-sm py-1.5">Login</Link>
                <Link to="/register" className="btn-primary text-sm py-1.5">Sign Up</Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  )
}
