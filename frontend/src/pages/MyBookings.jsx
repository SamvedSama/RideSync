import { useEffect, useState } from 'react'
import api from '../api'
import { BookOpen, MapPin, DollarSign, Clock } from 'lucide-react'
import { format } from 'date-fns'

const STATUS_STYLES = {
  CONFIRMED: 'bg-green-100 text-green-700',
  CANCELLED: 'bg-red-100 text-red-700',
}

export default function MyBookings() {
  const [bookings, setBookings] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => { fetchBookings() }, [])

  const fetchBookings = async () => {
    try {
      const { data } = await api.get('/bookings/my-bookings')
      setBookings(data)
    } finally { setLoading(false) }
  }

  const cancel = async (bookingId) => {
    if (!confirm('Cancel this booking?')) return
    try {
      await api.put(`/bookings/${bookingId}/cancel`)
      fetchBookings()
    } catch (err) {
      alert(err.response?.data?.error || 'Failed to cancel.')
    }
  }

  if (loading) return <div className="flex justify-center py-16"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-brand-600"></div></div>

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-slate-800">My Bookings</h1>
        <span className="badge bg-brand-100 text-brand-700">{bookings.length} total</span>
      </div>

      {bookings.length === 0 ? (
        <div className="text-center py-16 text-slate-400">
          <BookOpen size={48} className="mx-auto mb-3 text-slate-200" />
          <p className="font-medium text-slate-600">No bookings yet</p>
          <p className="text-sm mt-1">Search for rides and book your first trip!</p>
        </div>
      ) : (
        <div className="space-y-4">
          {bookings.map(b => (
            <div key={b.id} className="card">
              <div className="flex justify-between items-start">
                <div className="flex items-center gap-2 text-lg font-semibold text-slate-800">
                  <MapPin size={16} className="text-brand-500" />
                  {b.source} → {b.destination}
                </div>
                <span className={`badge ${STATUS_STYLES[b.status] || 'bg-slate-100 text-slate-600'}`}>
                  {b.status}
                </span>
              </div>

              <div className="mt-3 grid grid-cols-3 gap-3 text-sm text-slate-600">
                <div className="flex items-center gap-1.5">
                  <BookOpen size={13} className="text-slate-400" />
                  {b.seatsBooked} seat{b.seatsBooked > 1 ? 's' : ''}
                </div>
                <div className="flex items-center gap-1.5">
                  <DollarSign size={13} className="text-slate-400" />
                  ₹{b.totalFare} total
                </div>
                <div className="flex items-center gap-1.5 text-xs text-slate-400">
                  Booking #{b.id}
                </div>
              </div>

              {b.status === 'CONFIRMED' && (
                <div className="mt-4">
                  <button onClick={() => cancel(b.id)} className="btn-danger text-sm py-1.5">
                    Cancel Booking
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
