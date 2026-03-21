import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import api from '../api'
import { User, Phone, Mail, Shield } from 'lucide-react'

const ROLE_BADGE = {
  RIDER:  'bg-blue-100 text-blue-700',
  DRIVER: 'bg-green-100 text-green-700',
  ADMIN:  'bg-purple-100 text-purple-700',
}

export default function Profile() {
  const { user, updateUser } = useAuth()
  const [form, setForm] = useState({ name: user.name, phone: user.phone || '' })
  const [success, setSuccess] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess(false)
    setLoading(true)
    try {
      const { data } = await api.put(`/users/${user.userId}`, null, {
        params: { name: form.name, phone: form.phone }
      })
      updateUser({ name: data.name, phone: data.phone })
      setSuccess(true)
    } catch (err) {
      setError(err.response?.data?.error || 'Update failed.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-slate-800 mb-6">My Profile</h1>

      <div className="card mb-6">
        <div className="flex items-center gap-4 mb-4">
          <div className="w-14 h-14 rounded-full bg-brand-600 flex items-center justify-center text-white text-2xl font-bold">
            {user.name?.charAt(0).toUpperCase()}
          </div>
          <div>
            <div className="font-semibold text-slate-800 text-lg">{user.name}</div>
            <div className="flex items-center gap-2 mt-1">
              <Mail size={13} className="text-slate-400" />
              <span className="text-sm text-slate-500">{user.email}</span>
            </div>
          </div>
          <span className={`badge ml-auto ${ROLE_BADGE[user.role]}`}>
            <Shield size={11} className="mr-1" /> {user.role}
          </span>
        </div>

        <hr className="border-slate-100 mb-4" />

        {success && (
          <div className="bg-green-50 border border-green-200 text-green-700 text-sm rounded-lg px-4 py-3 mb-4">
            Profile updated successfully!
          </div>
        )}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 text-sm rounded-lg px-4 py-3 mb-4">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1 flex items-center gap-1.5">
              <User size={13} className="text-slate-400" /> Display Name
            </label>
            <input className="input" value={form.name}
              onChange={e => setForm(f => ({ ...f, name: e.target.value }))} required />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1 flex items-center gap-1.5">
              <Phone size={13} className="text-slate-400" /> Phone Number
            </label>
            <input className="input" placeholder="+91 98765 43210" value={form.phone}
              onChange={e => setForm(f => ({ ...f, phone: e.target.value }))} />
          </div>
          <button type="submit" disabled={loading} className="btn-primary">
            {loading ? 'Saving…' : 'Save Changes'}
          </button>
        </form>
      </div>

      <div className="card">
        <h2 className="font-semibold text-slate-700 mb-3">Account Info</h2>
        <div className="space-y-2 text-sm">
          <div className="flex justify-between"><span className="text-slate-500">User ID</span><span className="font-mono">{user.userId}</span></div>
          <div className="flex justify-between"><span className="text-slate-500">Email</span><span>{user.email}</span></div>
          <div className="flex justify-between"><span className="text-slate-500">Role</span><span className={`badge ${ROLE_BADGE[user.role]}`}>{user.role}</span></div>
          <div className="flex justify-between"><span className="text-slate-500">Status</span>
            <span className={`badge ${user.banned ? 'bg-red-100 text-red-700' : 'bg-green-100 text-green-700'}`}>
              {user.banned ? 'Banned' : 'Active'}
            </span>
          </div>
        </div>
      </div>
    </div>
  )
}
