import { useEffect, useState } from 'react'
import { getHealth } from '../services/api.js'

export default function HealthStatus() {
  const [health, setHealth] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    getHealth()
      .then(setHealth)
      .catch((err) => setError(err.message))
  }, [])

  if (error) {
    return <p className="error">Backend unreachable: {error}</p>
  }
  if (!health) {
    return <p className="muted">Checking backend…</p>
  }
  return (
    <p>
      Backend: <strong className={health.status === 'UP' ? 'ok' : 'error'}>{health.status}</strong>
      {' · '}
      Database: <strong className={health.database === 'UP' ? 'ok' : 'error'}>{health.database}</strong>
    </p>
  )
}
