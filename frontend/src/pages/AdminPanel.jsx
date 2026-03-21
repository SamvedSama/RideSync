import { useEffect, useState } from 'react'
import api from '../api'
import { Shield, Users, Car, FileText, Ban, CheckCircle, UserCog, XCircle } from 'lucide-react'
import { format } from 'date-fns'

const TABS = ['Users', 'Rides', 'Audit Log']

export default function AdminPanel() {
  const [tab, setTab] = useState('Users')
  const [users, setUsers] = useState([])
  const [rides, setRides] = useState([])
  const [auditLog, setAuditLog] = useState([])
  const [loading, setLoading] = useState(false)

  useEffect(() => { fetchAll() }, [])

  const fetchAll = async () => {
    setLoading(true)
    try {
      const [u, r, a] = await Promise.all([
        api.get('/admin/users'),
        api.get('/admin/rides'),
        api.get('/admin/audit'),
      ])
      setUsers(u.data)
      setRides(r.data)
      setAuditLog(a.data)
    } finally { setLoading(false) }
  }

  const banUser = async (userId, banned) => {
    try {
      await api.put(`/admin/users/${userId}/${banned ? 'unban' : 'ban'}`)
      fetchAll()
    } catch (err) { alert(err.response?.data?.error || 'Action failed.') }
  }

  const changeRole = async (userId, role) => {
    try {
      await api.put(`/admin/users/${userId}/role`, null, { params: { role } })
      fetchAll()
    } catch (err) { alert(err.response?.data?.error || 'Action failed.') }
  }

  const cancelRide = async (rideId) => {
    if (!confirm('Force-cancel this ride?')) return
    try {
      await api.put(`/admin/rides/${rideId}/cancel`)
      fetchAll()
    } catch (err) { alert(err.response?.data?.error || 'Action failed.') }
  }

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <div className="flex items-center gap-3 mb-6">
        <div className="w-10 h-10 rounded-xl bg-purple-100 flex items-center justify-center">
          <Shield size={20} className="text-purple-600" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Admin Panel</h1>
          <p className="text-sm text-slate-500">Manage users, rides and view audit logs</p>
        </div>
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-3 gap-4 mb-6">
        <div className="card text-center">
          <Users size={24} className="mx-auto text-brand-500 mb-1" />
          <div className="text-2xl font-bold">{users.length}</div>
          <div className="text-xs text-slate-500">Total Users</div>
        </div>
        <div className="card text-center">
          <Car size={24} className="mx-auto text-green-500 mb-1" />
          <div className="text-2xl font-bold">{rides.length}</div>
          <div className="text-xs text-slate-500">Total Rides</div>
        </div>
        <div className="card text-center">
          <FileText size={24} className="mx-auto text-amber-500 mb-1" />
          <div className="text-2xl font-bold">{auditLog.length}</div>
          <div className="text-xs text-slate-500">Audit Entries</div>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 mb-4 bg-slate-100 rounded-xl p-1 w-fit">
        {TABS.map(t => (
          <button key={t} onClick={() => setTab(t)}
            className={`px-4 py-1.5 rounded-lg text-sm font-medium transition-colors ${
              tab === t ? 'bg-white text-slate-800 shadow-sm' : 'text-slate-500 hover:text-slate-700'
            }`}>
            {t}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="flex justify-center py-12"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-brand-600"></div></div>
      ) : (
        <>
          {/* Users tab */}
          {tab === 'Users' && (
            <div className="card overflow-hidden p-0">
              <table className="w-full text-sm">
                <thead className="bg-slate-50 border-b border-slate-100">
                  <tr>
                    {['ID', 'Name', 'Email', 'Phone', 'Role', 'Status', 'Actions'].map(h => (
                      <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wide">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-50">
                  {users.map(u => (
                    <tr key={u.userId} className="hover:bg-slate-50 transition-colors">
                      <td className="px-4 py-3 font-mono text-slate-400 text-xs">{u.userId}</td>
                      <td className="px-4 py-3 font-medium">{u.name}</td>
                      <td className="px-4 py-3 text-slate-500">{u.email}</td>
                      <td className="px-4 py-3 text-slate-500">{u.phone || '—'}</td>
                      <td className="px-4 py-3">
                        <select value={u.role} onChange={e => changeRole(u.userId, e.target.value)}
                          className="text-xs border border-slate-200 rounded px-1.5 py-0.5">
                          {['RIDER','DRIVER','ADMIN'].map(r => <option key={r} value={r}>{r}</option>)}
                        </select>
                      </td>
                      <td className="px-4 py-3">
                        <span className={`badge text-xs ${u.banned ? 'bg-red-100 text-red-700' : 'bg-green-100 text-green-700'}`}>
                          {u.banned ? 'Banned' : 'Active'}
                        </span>
                      </td>
                      <td className="px-4 py-3">
                        {u.role !== 'ADMIN' && (
                          <button onClick={() => banUser(u.userId, u.banned)}
                            className={`flex items-center gap-1 text-xs px-2 py-1 rounded-lg font-medium transition-colors ${
                              u.banned
                                ? 'bg-green-100 text-green-700 hover:bg-green-200'
                                : 'bg-red-100 text-red-700 hover:bg-red-200'
                            }`}>
                            {u.banned ? <><CheckCircle size={12} /> Unban</> : <><Ban size={12} /> Ban</>}
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Rides tab */}
          {tab === 'Rides' && (
            <div className="card overflow-hidden p-0">
              <table className="w-full text-sm">
                <thead className="bg-slate-50 border-b border-slate-100">
                  <tr>
                    {['ID', 'Route', 'Driver', 'Seats', 'Fare', 'Status', 'Action'].map(h => (
                      <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wide">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-50">
                  {rides.map(r => (
                    <tr key={r.id} className="hover:bg-slate-50 transition-colors">
                      <td className="px-4 py-3 font-mono text-slate-400 text-xs">{r.id}</td>
                      <td className="px-4 py-3 font-medium">{r.source} → {r.destination}</td>
                      <td className="px-4 py-3 text-slate-500">{r.driver?.name}</td>
                      <td className="px-4 py-3 text-slate-500">{r.totalSeats}</td>
                      <td className="px-4 py-3 text-slate-600 font-medium">₹{r.farePerSeat}</td>
                      <td className="px-4 py-3">
                        <span className={`badge text-xs ${
                          r.status === 'CANCELLED' ? 'bg-red-100 text-red-700' :
                          r.status === 'COMPLETED' ? 'bg-slate-100 text-slate-600' :
                          'bg-green-100 text-green-700'
                        }`}>{r.status}</span>
                      </td>
                      <td className="px-4 py-3">
                        {!['CANCELLED','COMPLETED'].includes(r.status) && (
                          <button onClick={() => cancelRide(r.id)}
                            className="flex items-center gap-1 text-xs px-2 py-1 rounded-lg font-medium bg-red-100 text-red-700 hover:bg-red-200 transition-colors">
                            <XCircle size={12} /> Cancel
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Audit Log tab */}
          {tab === 'Audit Log' && (
            <div className="card overflow-hidden p-0">
              <table className="w-full text-sm">
                <thead className="bg-slate-50 border-b border-slate-100">
                  <tr>
                    {['Time', 'Admin ID', 'Action', 'Target', 'Notes'].map(h => (
                      <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-slate-500 uppercase tracking-wide">{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-50">
                  {[...auditLog].reverse().map(entry => (
                    <tr key={entry.id} className="hover:bg-slate-50 transition-colors">
                      <td className="px-4 py-3 text-xs text-slate-400 whitespace-nowrap">
                        {format(new Date(entry.performedAt), 'MMM d, h:mm a')}
                      </td>
                      <td className="px-4 py-3 font-mono text-xs">{entry.adminId}</td>
                      <td className="px-4 py-3">
                        <span className="badge bg-purple-100 text-purple-700 text-xs">{entry.action}</span>
                      </td>
                      <td className="px-4 py-3 text-slate-500 text-xs">{entry.targetType} #{entry.targetId}</td>
                      <td className="px-4 py-3 text-slate-500 text-xs">{entry.notes}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
    </div>
  )
}
