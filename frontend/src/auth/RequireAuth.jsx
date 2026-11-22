import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from './useAuth.js'

// Shows its content only to logged-in users (and only to admins with adminOnly).
// The backend checks the same rules; this only avoids showing pages that would fail.
export default function RequireAuth({ adminOnly = false, children }) {
  const { account, isAdmin } = useAuth()
  const location = useLocation()

  if (!account) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }
  if (adminOnly && !isAdmin) {
    return (
      <section>
        <h1>No permission</h1>
        <p>This page is only available to administrators.</p>
      </section>
    )
  }
  return children ?? <Outlet />
}
