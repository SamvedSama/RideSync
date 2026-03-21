import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import api from '../api'
import { Car, BookOpen, TrendingUp, Clock, PlusCircle, Search } from 'lucide-react'

export default function Dashboard() {
  const { user } = useAuth()
  const [stats, setStats] = useState(null)

  useEffect(() => {
    const fetchStats = async () => {
      try {
        if (user.role === 'DRIVER') {
          const { data } = await api.get('/rides/my-rides')
          const active = data.filter(r => ['PUBLISHED','BOOKED','IN_PROGRESS'].includes(r.status)).length
          const completed = data.filter(r => r.status === 'COMPLETED').length
          const totalEarnings = data
            .filter(r => r.status === 'COMPLETED')
            .reduce((sum, r) => sum + r.farePerSeat * r.totalSeats, 0)
          setStats({ total: data.length, active, completed, totalEarnings })
        } else if (user.role === 'RIDER') {
          const { data } = await api.get('/bookings/my-bookings')
          const confirmed = data.filter(b => b.status === 'CONFIRMED').length
          const cancelled = data.filter(b => b.status === 'CANCELLED').length
          const spent = data
            .filter(b => b.status === 'CONFIRMED')
            .reduce((sum, b) => sum + b.totalFare, 0)
          setStats({ total: data.length, confirmed, cancelled, spent })
        }
      } catch {/* ignore */}
    }
    fetchStats()
  }, [user])

  const greeting = () => {
    const h = new Date().getHours()
    if (h < 12) return 'Good morning'
    if (h < 17) return 'Good afternoon'
    return 'Good evening'
  }

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-slate-800">{greeting()}, {user.name.split(' ')[0]}! 👋</h1>
        <p className="text-slate-500 mt-1">
          You're signed in as a <span className="font-semibold capitalize">{user.role.toLowerCase()}</span>.
        </p>
      </div>

      {/* Quick actions */}
      <div className="grid sm:grid-cols-2 gap-4 mb-8">
        {user.role === 'DRIVER' ? (
          <>
            <Link to="/rides/create" className="card hover:shadow-md transition flex items-center gap-4 cursor-pointer border-brand-200 hover:border-brand-400">
              <div className="w-12 h-12 rounded-xl bg-brand-100 flex items-center justify-center text-brand-600">
                <PlusCircle size={24} />
              </div>
              <div>
                <div className="font-semibold text-slate-800">Post a New Ride</div>
                <div className="text-sm text-slate-500">Share your route with others</div>
              </div>
            </Link>
            <Link to="/my-rides" className="card hover:shadow-md transition flex items-center gap-4 cursor-pointer">
              <div className="w-12 h-12 rounded-xl bg-green-100 flex items-center justify-center text-green-600">
                <Car size={24} />
              </div>
              <div>
                <div className="font-semibold text-slate-800">Manage My Rides</div>
                <div className="text-sm text-slate-500">Update status, view bookings</div>
              </div>
            </Link>
          </>
        ) : user.role === 'RIDER' ? (
          <>
            <Link to="/rides/search" className="card hover:shadow-md transition flex items-center gap-4 cursor-pointer border-brand-200 hover:border-brand-400">
              <div className="w-12 h-12 rounded-xl bg-brand-100 flex items-center justify-center text-brand-600">
                <Search size={24} />
              </div>
              <div>
                <div className="font-semibold text-slate-800">Find a Ride</div>
                <div className="text-sm text-slate-500">Search available rides near you</div>
              </div>
            </Link>
            <Link to="/my-bookings" className="card hover:shadow-md transition flex items-center gap-4 cursor-pointer">
              <div className="w-12 h-12 rounded-xl bg-purple-100 flex items-center justify-center text-purple-600">
                <BookOpen size={24} />
              </div>
              <div>
                <div className="font-semibold text-slate-800">My Bookings</div>
                <div className="text-sm text-slate-500">View and manage your trips</div>
              </div>
            </Link>
          </>
        ) : null}
      </div>

      {/* Stats */}
      {stats && (
        <div>
          <h2 className="text-lg font-semibold text-slate-700 mb-4">Your Stats</h2>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            {user.role === 'DRIVER' ? (
              <>
                <StatCard label="Total Rides" value={stats.total} icon={Car} color="brand" />
                <StatCard label="Active" value={stats.active} icon={Clock} color="green" />
                <StatCard label="Completed" value={stats.completed} icon={TrendingUp} color="purple" />
                <StatCard label="Est. Earnings" value={`₹${stats.totalEarnings}`} icon={TrendingUp} color="amber" />
              </>
            ) : (
              <>
                <StatCard label="Total Bookings" value={stats.total} icon={BookOpen} color="brand" />
                <StatCard label="Confirmed" value={stats.confirmed} icon={Clock} color="green" />
                <StatCard label="Cancelled" value={stats.cancelled} icon={TrendingUp} color="red" />
                <StatCard label="Total Spent" value={`₹${stats.spent}`} icon={TrendingUp} color="amber" />
              </>
            )}
          </div>
        </div>
      )}
    </div>
  )
}

function StatCard({ label, value, icon: Icon, color }) {
  const colors = {
    brand: 'bg-brand-50 text-brand-600',
    green: 'bg-green-50 text-green-600',
    purple: 'bg-purple-50 text-purple-600',
    amber: 'bg-amber-50 text-amber-600',
    red: 'bg-red-50 text-red-600',
  }
  return (
    <div className="card">
      <div className={`w-9 h-9 rounded-lg ${colors[color]} flex items-center justify-center mb-3`}>
        <Icon size={18} />
      </div>
      <div className="text-2xl font-bold text-slate-800">{value}</div>
      <div className="text-xs text-slate-500 mt-0.5">{label}</div>
    </div>
  )
}
