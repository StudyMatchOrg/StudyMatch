import { NavLink, Outlet } from 'react-router-dom'
import HealthStatus from './HealthStatus.jsx'

export default function Layout() {
  return (
    <>
      <header className="topbar">
        <div className="topbar-inner">
          <span className="brand">StudyMatch</span>
          <nav>
            <NavLink to="/students">Students</NavLink>
            <NavLink to="/competencies">Competencies</NavLink>
            <NavLink to="/import">Import</NavLink>
          </nav>
          <HealthStatus />
        </div>
      </header>
      <main>
        <Outlet />
      </main>
    </>
  )
}
