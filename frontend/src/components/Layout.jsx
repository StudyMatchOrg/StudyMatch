import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/useAuth.js'
import HealthStatus from './HealthStatus.jsx'

export default function Layout() {
  const { account, isAdmin, logout } = useAuth()

  return (
    <>
      <header className="topbar">
        <div className="topbar-inner">
          <span className="brand">StudyMatch</span>
          <nav>
            <NavLink to="/students">Students</NavLink>
            <NavLink to="/projects">Projects</NavLink>
            <NavLink to="/competencies">Competencies</NavLink>
            <NavLink to="/import">Import</NavLink>
            {isAdmin && <NavLink to="/structure">Structure</NavLink>}
            {isAdmin && <NavLink to="/accounts">Accounts</NavLink>}
          </nav>
          <HealthStatus />
          <div className="user-menu">
            <span>{account.displayName}</span>
            <span className={isAdmin ? 'badge badge-accent' : 'badge'}>{isAdmin ? 'Admin' : 'Teacher'}</span>
            <button type="button" className="btn-secondary btn-small" onClick={logout}>
              Log out
            </button>
          </div>
        </div>
      </header>
      <main>
        <Outlet />
      </main>
    </>
  )
}
