import { MapPin, Clock, Users, DollarSign, User } from 'lucide-react'
import { format } from 'date-fns'

const STATUS_STYLES = {
  PUBLISHED: 'bg-green-100 text-green-700',
  BOOKED:    'bg-amber-100 text-amber-700',
  IN_PROGRESS: 'bg-blue-100 text-blue-700',
  COMPLETED: 'bg-slate-100 text-slate-600',
  CANCELLED: 'bg-red-100 text-red-700',
  CREATED:   'bg-purple-100 text-purple-700',
}

export default function RideCard({ ride, actions }) {
  const departure = ride.departureTime
    ? format(new Date(ride.departureTime), 'MMM d, h:mm a')
    : 'Unknown'
  const availableSeats = ride.availableSeats ?? ride.totalSeats

  return (
    <div className="card hover:shadow-md transition-shadow">
      <div className="flex justify-between items-start mb-3">
        <div>
          <div className="flex items-center gap-2 text-lg font-semibold text-slate-800">
            <MapPin size={16} className="text-brand-500 shrink-0" />
            <span>{ride.source}</span>
            <span className="text-slate-400">→</span>
            <span>{ride.destination}</span>
          </div>
          {ride.driver && (
            <div className="flex items-center gap-1 mt-1 text-xs text-slate-500">
              <User size={12} />
              <span>{ride.driver.name}</span>
            </div>
          )}
        </div>
        <span className={`badge ${STATUS_STYLES[ride.status] || 'bg-slate-100 text-slate-600'}`}>
          {ride.status?.replace('_', ' ')}
        </span>
      </div>

      <div className="grid grid-cols-3 gap-3 mt-4">
        <div className="flex items-center gap-1.5 text-sm text-slate-600">
          <Clock size={14} className="text-slate-400" />
          <span>{departure}</span>
        </div>
        <div className="flex items-center gap-1.5 text-sm text-slate-600">
          <Users size={14} className="text-slate-400" />
          <span>{availableSeats} / {ride.totalSeats} seats</span>
        </div>
        <div className="flex items-center gap-1.5 text-sm font-semibold text-brand-700">
          <DollarSign size={14} />
          <span>₹{ride.farePerSeat} / seat</span>
        </div>
      </div>

      {actions && <div className="mt-4 flex gap-2 flex-wrap">{actions}</div>}
    </div>
  )
}
