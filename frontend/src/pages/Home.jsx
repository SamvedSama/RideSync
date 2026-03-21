import { Link } from 'react-router-dom'
import { Car, Shield, DollarSign, Users, ArrowRight } from 'lucide-react'
import { useAuth } from '../context/AuthContext'

export default function Home() {
  const { user } = useAuth()

  return (
    <div className="min-h-screen">
      {/* Hero */}
      <div className="bg-gradient-to-br from-brand-700 via-brand-600 to-brand-500 text-white">
        <div className="max-w-5xl mx-auto px-4 py-20 text-center">
          <div className="inline-flex items-center gap-2 bg-white/20 rounded-full px-4 py-1.5 text-sm font-medium mb-6">
            <Car size={16} /> College Carpooling Made Easy
          </div>
          <h1 className="text-5xl font-bold mb-4 leading-tight">
            Share Rides,<br/>Save Money & Planet
          </h1>
          <p className="text-brand-100 text-xl mb-10 max-w-2xl mx-auto">
            RideSync connects students and staff for safe, affordable carpooling
            across campus and beyond.
          </p>
          <div className="flex gap-4 justify-center">
            {user ? (
              <Link to="/dashboard" className="bg-white text-brand-700 font-bold px-6 py-3 rounded-xl hover:bg-brand-50 transition flex items-center gap-2">
                Go to Dashboard <ArrowRight size={18} />
              </Link>
            ) : (
              <>
                <Link to="/register" className="bg-white text-brand-700 font-bold px-6 py-3 rounded-xl hover:bg-brand-50 transition flex items-center gap-2">
                  Get Started <ArrowRight size={18} />
                </Link>
                <Link to="/rides/search" className="border border-white/50 text-white font-bold px-6 py-3 rounded-xl hover:bg-white/10 transition">
                  Browse Rides
                </Link>
              </>
            )}
          </div>
        </div>
      </div>

      {/* Features */}
      <div className="max-w-5xl mx-auto px-4 py-16">
        <h2 className="text-2xl font-bold text-center text-slate-800 mb-10">Why RideSync?</h2>
        <div className="grid md:grid-cols-3 gap-6">
          {[
            { icon: DollarSign, title: 'Save Money', desc: 'Split fuel costs with fellow commuters and save up to 70% on travel expenses.', color: 'text-green-600 bg-green-50' },
            { icon: Shield, title: 'Safe & Verified', desc: 'All users are verified members. Admin oversight and audit logs keep the community safe.', color: 'text-brand-600 bg-brand-50' },
            { icon: Users, title: 'Community First', desc: 'Connect with peers, make new friends, and build a sustainable campus commute culture.', color: 'text-purple-600 bg-purple-50' },
          ].map(({ icon: Icon, title, desc, color }) => (
            <div key={title} className="card text-center">
              <div className={`w-12 h-12 rounded-xl ${color} flex items-center justify-center mx-auto mb-4`}>
                <Icon size={24} />
              </div>
              <h3 className="font-bold text-slate-800 mb-2">{title}</h3>
              <p className="text-sm text-slate-500">{desc}</p>
            </div>
          ))}
        </div>
      </div>

      {/* CTA */}
      {!user && (
        <div className="bg-slate-800 text-white py-14 text-center">
          <h2 className="text-3xl font-bold mb-4">Ready to ride smarter?</h2>
          <p className="text-slate-400 mb-8">Join hundreds of commuters already using RideSync.</p>
          <Link to="/register" className="btn-primary bg-brand-500 hover:bg-brand-400 px-8 py-3 text-base">
            Create Free Account
          </Link>
        </div>
      )}
    </div>
  )
}
