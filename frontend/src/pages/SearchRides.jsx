import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api'
import RideCard from '../components/RideCard'
import { Search, MapPin } from 'lucide-react'
import { useAuth } from '../context/AuthContext'

export default function SearchRides() {
  const [form, setForm] = useState({ source: '', destination: '' })
  const [rides, setRides] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [booking, setBooking] = useState({})
  const { user } = useAuth()
  const navigate = useNavigate()

  const handleSearch = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    setRides(null)
    try {
      const { data } = await api.get('/rides/search', { params: form })
      setRides(data)
    } catch {
      setError('Search failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const handleBook = async (rideId) => {
    if (!user) { navigate('/login'); return }
    const seats = booking[rideId] || 1
    try {
      await api.post('/bookings', null, { params: { rideId, seats } })
      alert('Ride booked successfully! Check My Bookings.')
      handleSearch({ preventDefault: () => {} })
    } catch (err) {
      alert(err.response?.data?.error || 'Booking failed.')
    }
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-slate-800 mb-6">Find a Ride</h1>

      <div className="card mb-6">
        <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-3">
          <div className="relative flex-1">
            <MapPin size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input className="input pl-9" placeholder="From (e.g. Koramangala)"
              value={form.source} onChange={e => setForm(f => ({ ...f, source: e.target.value }))} required />
          </div>
          <div className="relative flex-1">
            <MapPin size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-brand-400" />
            <input className="input pl-9" placeholder="To (e.g. Whitefield)"
              value={form.destination} onChange={e => setForm(f => ({ ...f, destination: e.target.value }))} required />
          </div>
          <button type="submit" disabled={loading} className="btn-primary flex items-center gap-2 whitespace-nowrap">
            <Search size={16} /> {loading ? 'Searching…' : 'Search'}
          </button>
        </form>
      </div>

      {error && <div className="bg-red-50 text-red-700 rounded-lg px-4 py-3 mb-4 text-sm">{error}</div>}

      {rides !== null && (
        rides.length === 0 ? (
          <div className="text-center py-12 text-slate-500">
            <Search size={40} className="mx-auto mb-3 text-slate-300" />
            <p className="font-medium">No rides found</p>
            <p className="text-sm mt-1">Try different source/destination or check back later.</p>
          </div>
        ) : (
          <div className="space-y-4">
            <p className="text-sm text-slate-500">{rides.length} ride{rides.length !== 1 ? 's' : ''} found</p>
            {rides.map(ride => (
              <RideCard key={ride.id} ride={ride} actions={
                user?.role === 'RIDER' ? [
                  <div key="book" className="flex items-center gap-2">
                    <select
                      className="input w-20 py-1.5 text-sm"
                      value={booking[ride.id] || 1}
                      onChange={e => setBooking(b => ({ ...b, [ride.id]: parseInt(e.target.value) }))}
                    >
                      {Array.from({ length: Math.min(ride.availableSeats ?? ride.totalSeats, 5) }, (_, i) => i + 1)
                        .map(n => <option key={n} value={n}>{n} seat{n > 1 ? 's' : ''}</option>)}
                    </select>
                    <button onClick={() => handleBook(ride.id)} className="btn-primary text-sm py-1.5">
                      Book — ₹{(ride.farePerSeat * (booking[ride.id] || 1)).toFixed(0)}
                    </button>
                  </div>
                ] : user ? [] : [
                  <button key="login" onClick={() => navigate('/login')} className="btn-secondary text-sm">
                    Login to Book
                  </button>
                ]
              } />
            ))}
          </div>
        )
      )}
    </div>
  )
}
