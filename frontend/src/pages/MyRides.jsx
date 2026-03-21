import { useEffect, useState } from 'react'
import api from '../api'
import RideCard from '../components/RideCard'
import { Car, Users } from 'lucide-react'

const STATUSES = ['PUBLISHED', 'BOOKED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED']

export default function MyRides() {
  const [rides, setRides] = useState([])
  const [loading, setLoading] = useState(true)
  const [bookingsMap, setBookingsMap] = useState({})
  const [expandedRide, setExpandedRide] = useState(null)

  useEffect(() => { fetchRides() }, [])

  const fetchRides = async () => {
    try {
      const { data } = await api.get('/rides/my-rides')
      setRides(data)
    } finally { setLoading(false) }
  }

  const updateStatus = async (rideId, status) => {
    try {
      await api.put(`/rides/${rideId}/status`, null, { params: { status } })
      fetchRides()
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to update status.')
    }
  }

  const toggleBookings = async (rideId) => {
    if (expandedRide === rideId) { setExpandedRide(null); return }
    setExpandedRide(rideId)
    if (!bookingsMap[rideId]) {
      try {
        const { data } = await api.get(`/bookings/ride/${rideId}`)
        setBookingsMap(m => ({ ...m, [rideId]: data }))
      } catch { /* ignore */ }
    }
  }

  if (loading) return <div className="flex justify-center py-16"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-brand-600"></div></div>

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-slate-800">My Rides</h1>
        <span className="badge bg-brand-100 text-brand-700">{rides.length} total</span>
      </div>

      {rides.length === 0 ? (
        <div className="text-center py-16 text-slate-400">
          <Car size={48} className="mx-auto mb-3 text-slate-200" />
          <p className="font-medium text-slate-600">No rides posted yet</p>
          <p className="text-sm mt-1">Post your first ride to start earning!</p>
        </div>
      ) : (
        <div className="space-y-4">
          {rides.map(ride => (
            <div key={ride.id}>
              <RideCard ride={ride} actions={[
                <select key="status" className="input w-auto py-1 text-sm"
                  value={ride.status}
                  onChange={e => updateStatus(ride.id, e.target.value)}>
                  {STATUSES.map(s => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
                </select>,
                <button key="bookings" onClick={() => toggleBookings(ride.id)}
                  className="btn-secondary text-sm py-1 flex items-center gap-1">
                  <Users size={14} /> {expandedRide === ride.id ? 'Hide' : 'View'} Bookings
                </button>
              ]} />

              {expandedRide === ride.id && (
                <div className="ml-4 mt-2 border-l-2 border-brand-100 pl-4">
                  {!bookingsMap[ride.id] ? (
                    <div className="text-sm text-slate-400 py-2">Loading bookings…</div>
                  ) : bookingsMap[ride.id].length === 0 ? (
                    <div className="text-sm text-slate-400 py-2">No bookings yet.</div>
                  ) : (
                    <div className="space-y-2">
                      {bookingsMap[ride.id].map(b => (
                        <div key={b.id} className="bg-slate-50 rounded-lg p-3 text-sm flex justify-between items-center">
                          <div>
                            <span className="font-medium">{b.riderName}</span>
                            <span className="text-slate-500 ml-2">· {b.seatsBooked} seat{b.seatsBooked > 1 ? 's' : ''}</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <span className="font-semibold text-brand-700">₹{b.totalFare}</span>
                            <span className={`badge text-xs ${b.status === 'CONFIRMED' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                              {b.status}
                            </span>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
