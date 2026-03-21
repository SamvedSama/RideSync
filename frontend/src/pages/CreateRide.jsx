import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api'
import { MapPin, Clock, Users, DollarSign, PlusCircle } from 'lucide-react'

export default function CreateRide() {
  const [form, setForm] = useState({
    source: '', destination: '', totalSeats: 2,
    farePerSeat: 50, departureTime: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await api.post('/rides', {
        ...form,
        totalSeats: parseInt(form.totalSeats),
        farePerSeat: parseFloat(form.farePerSeat),
      })
      navigate('/my-rides')
    } catch (err) {
      const d = err.response?.data
      setError(typeof d === 'object' ? Object.values(d).join('. ') : 'Failed to create ride.')
    } finally {
      setLoading(false)
    }
  }

  // Minimum datetime = now
  const minDateTime = new Date().toISOString().slice(0, 16)

  return (
    <div className="max-w-xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-slate-800 mb-6">Post a New Ride</h1>

      <div className="card">
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3 mb-4">{error}</div>
        )}
        <form onSubmit={handleSubmit} className="space-y-5">
          <div className="grid grid-cols-1 gap-5">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1 flex items-center gap-1.5">
                <MapPin size={14} className="text-slate-400" /> From
              </label>
              <input className="input" placeholder="e.g. Koramangala" value={form.source}
                onChange={e => setForm(f => ({ ...f, source: e.target.value }))} required />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1 flex items-center gap-1.5">
                <MapPin size={14} className="text-brand-500" /> To
              </label>
              <input className="input" placeholder="e.g. Whitefield" value={form.destination}
                onChange={e => setForm(f => ({ ...f, destination: e.target.value }))} required />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1 flex items-center gap-1.5">
              <Clock size={14} className="text-slate-400" /> Departure Time
            </label>
            <input type="datetime-local" className="input" min={minDateTime}
              value={form.departureTime}
              onChange={e => setForm(f => ({ ...f, departureTime: e.target.value }))} required />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1 flex items-center gap-1.5">
                <Users size={14} className="text-slate-400" /> Total Seats
              </label>
              <input type="number" className="input" min="1" max="8"
                value={form.totalSeats}
                onChange={e => setForm(f => ({ ...f, totalSeats: e.target.value }))} required />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1 flex items-center gap-1.5">
                <DollarSign size={14} className="text-slate-400" /> Fare per Seat (₹)
              </label>
              <input type="number" className="input" min="0" step="5"
                value={form.farePerSeat}
                onChange={e => setForm(f => ({ ...f, farePerSeat: e.target.value }))} required />
            </div>
          </div>

          <div className="bg-brand-50 rounded-xl p-4 text-sm text-brand-700">
            <strong>Estimated earnings:</strong> ₹{(form.farePerSeat * form.totalSeats).toFixed(0)} if fully booked
          </div>

          <button type="submit" disabled={loading} className="btn-primary w-full py-2.5 flex items-center justify-center gap-2">
            <PlusCircle size={18} /> {loading ? 'Posting…' : 'Post Ride'}
          </button>
        </form>
      </div>
    </div>
  )
}
