import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { generateGroups, getGroupSets, getStrategies } from '../../services/groupSetsApi.js'

const format = (value) => value.toFixed(2)

// On the project page: generate groups, and every earlier generation of this project.
export default function GroupGenerationSection({ project }) {
  const navigate = useNavigate()
  const [strategies, setStrategies] = useState([])
  const [strategy, setStrategy] = useState('BALANCED')
  const [history, setHistory] = useState([])
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    Promise.all([getStrategies(), getGroupSets(project.id)])
      .then(([loadedStrategies, loadedHistory]) => {
        setStrategies(loadedStrategies)
        setHistory(loadedHistory)
      })
      .catch((err) => setError(err.message))
  }, [project.id])

  async function handleGenerate(event) {
    event.preventDefault()
    setError(null)
    setBusy(true)
    try {
      const generated = await generateGroups(project.id, strategy)
      navigate(`/group-sets/${generated.id}`)
    } catch (err) {
      setError(err.message)
      setBusy(false)
    }
  }

  const description = strategies.find((s) => s.code === strategy)?.description

  return (
    <section>
      <h2>Groups</h2>
      <form onSubmit={handleGenerate} className="actions">
        <label>
          Strategy
          <select value={strategy} onChange={(e) => setStrategy(e.target.value)}>
            {strategies.map((s) => (
              <option key={s.code} value={s.code}>
                {s.code === 'BALANCED' ? 'Balanced and complementary' : 'Random'}
              </option>
            ))}
          </select>
        </label>
        <button type="submit" disabled={busy || project.students.length < 2}>
          {busy ? 'Generating…' : 'Generate groups'}
        </button>
      </form>
      {description && <p className="muted small">{description}</p>}
      <p className="muted small">
        Groups use the students&apos; current profiles. Every generation is saved, so you can change grades or the
        project and generate again without losing the earlier groups.
      </p>
      {error && <p className="alert alert-error">{error}</p>}

      <h3>Earlier generations</h3>
      {history.length === 0 ? (
        <p className="muted">No groups generated yet.</p>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Generated</th>
                <th>Strategy</th>
                <th>By</th>
                <th className="number">Groups</th>
                <th className="number">Weakest group</th>
                <th className="number">Random groups</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {history.map((set) => (
                <tr key={set.id}>
                  <td>{new Date(set.generatedAt).toLocaleString('en-GB')}</td>
                  <td>{set.strategy === 'BALANCED' ? 'Balanced' : 'Random'}</td>
                  <td>{set.generatedBy}</td>
                  <td className="number">
                    {set.groupCount} <span className="muted small">(size {set.groupSize})</span>
                  </td>
                  <td className="number">{format(set.weakestScore)}</td>
                  <td className="number muted">{format(set.randomWeakestScore)}</td>
                  <td>
                    <Link to={`/group-sets/${set.id}`}>View</Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
