import { useCallback, useEffect, useState } from 'react'
import { getCourseUnits } from '../services/academicApi.js'
import {
  changeWeight,
  createCompetency,
  getCompetencies,
  getCourseUnitCompetencies,
  linkCourseUnit,
  unlinkCourseUnit,
  updateCompetency,
} from '../services/competenciesApi.js'

// Turns the list of (course unit, competency, weight) links into one row per course unit.
function toRows(links) {
  const rows = new Map()
  for (const link of links) {
    if (!rows.has(link.courseUnitId)) {
      rows.set(link.courseUnitId, {
        id: link.courseUnitId,
        code: link.courseUnitCode,
        name: link.courseUnitName,
        ects: link.ects,
        links: {},
      })
    }
    rows.get(link.courseUnitId).links[link.competency.id] = link
  }
  return [...rows.values()]
}

const EMPTY_COMPETENCY = { code: '', name: '', description: '' }

export default function CompetenciesPage() {
  const [competencies, setCompetencies] = useState([])
  const [links, setLinks] = useState([])
  const [courseUnits, setCourseUnits] = useState([])
  const [newCompetency, setNewCompetency] = useState(EMPTY_COMPETENCY)
  const [weightForm, setWeightForm] = useState({ courseUnitId: '', competencyId: '', weight: '' })
  const [editing, setEditing] = useState(null)
  const [message, setMessage] = useState(null)
  const [error, setError] = useState(null)

  const load = useCallback(
    () =>
      Promise.all([getCompetencies(), getCourseUnitCompetencies(), getCourseUnits()])
        .then(([loadedCompetencies, loadedLinks, units]) => {
          setCompetencies(loadedCompetencies)
          setLinks(loadedLinks)
          setCourseUnits(units)
        })
        .catch((err) => setError(err.message)),
    [],
  )

  useEffect(() => {
    load()
  }, [load])

  // Runs a change, shows its result and reloads the tables.
  async function run(action, success) {
    setError(null)
    setMessage(null)
    try {
      await action()
      setMessage(success)
      await load()
      return true
    } catch (err) {
      setError(err.message)
      return false
    }
  }

  const existingLink = links.find(
    (l) => l.courseUnitId === Number(weightForm.courseUnitId) && l.competency.id === Number(weightForm.competencyId),
  )

  function chooseLink(courseUnitId, competencyId) {
    const link = links.find((l) => l.courseUnitId === Number(courseUnitId) && l.competency.id === Number(competencyId))
    setWeightForm({ courseUnitId, competencyId, weight: link ? String(link.weight) : '' })
  }

  async function handleCreateCompetency(event) {
    event.preventDefault()
    if (await run(() => createCompetency(newCompetency), `Competency ${newCompetency.name} created.`)) {
      setNewCompetency(EMPTY_COMPETENCY)
    }
  }

  async function handleSaveWeight(event) {
    event.preventDefault()
    const weight = Number(weightForm.weight)
    await run(
      () =>
        existingLink
          ? changeWeight(existingLink.id, weight)
          : linkCourseUnit(Number(weightForm.courseUnitId), Number(weightForm.competencyId), weight),
      'Weight saved. Student profiles already use it.',
    )
  }

  async function handleUnlink() {
    if (await run(() => unlinkCourseUnit(existingLink.id), 'Link removed.')) {
      setWeightForm({ ...weightForm, weight: '' })
    }
  }

  async function handleSaveCompetency(event) {
    event.preventDefault()
    if (await run(() => updateCompetency(editing.id, editing), 'Competency saved.')) {
      setEditing(null)
    }
  }

  const rows = toRows(links)

  return (
    <>
      <h1>Competencies</h1>

      <section>
        <h2>How grades become competencies</h2>
        <p>
          Each course unit develops one or more competencies with a weight between 0 and 1. A grade counts as
          evidence for each of those competencies, worth <strong>weight × ECTS / 6</strong>: a 6-ECTS course unit with
          weight 1 is one full piece of evidence. A student&apos;s score in a competency is the weighted average of
          (grade / 20) over that evidence, using the best grade of each course unit.
        </p>
      </section>

      {message && <p className="alert alert-success">{message}</p>}
      {error && <p className="alert alert-error">{error}</p>}

      <section>
        <h2>Course-unit weights</h2>
        <p className="muted small">Click a cell to change its weight.</p>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Course unit</th>
                <th className="number">ECTS</th>
                {competencies.map((competency) => (
                  <th key={competency.id} className="number" title={competency.description}>
                    {competency.name}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {rows.map((row) => (
                <tr key={row.id}>
                  <td>
                    <strong>{row.code}</strong> <span className="muted small">{row.name}</span>
                  </td>
                  <td className="number">{row.ects}</td>
                  {competencies.map((competency) => (
                    <td key={competency.id} className="number">
                      <button
                        type="button"
                        className="btn-secondary btn-small"
                        onClick={() => chooseLink(row.id, competency.id)}
                      >
                        {row.links[competency.id]?.weight ?? '–'}
                      </button>
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <h3>Change a weight</h3>
        <form onSubmit={handleSaveWeight}>
          <div className="form-grid">
            <label>
              Course unit
              <select
                value={weightForm.courseUnitId}
                onChange={(e) => chooseLink(e.target.value, weightForm.competencyId)}
                required
              >
                <option value="">Choose</option>
                {courseUnits.map((unit) => (
                  <option key={unit.id} value={unit.id}>
                    {unit.code} – {unit.name}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Competency
              <select
                value={weightForm.competencyId}
                onChange={(e) => chooseLink(weightForm.courseUnitId, e.target.value)}
                required
              >
                <option value="">Choose</option>
                {competencies.map((competency) => (
                  <option key={competency.id} value={competency.id}>
                    {competency.name}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Weight (greater than 0, at most 1)
              <input
                type="number"
                min="0.05"
                max="1"
                step="0.05"
                value={weightForm.weight}
                onChange={(e) => setWeightForm({ ...weightForm, weight: e.target.value })}
                required
              />
            </label>
          </div>
          <div className="actions">
            <button type="submit">{existingLink ? 'Change weight' : 'Link with this weight'}</button>
            {existingLink && (
              <button type="button" className="btn-danger" onClick={handleUnlink}>
                Remove link
              </button>
            )}
          </div>
        </form>
      </section>

      <section>
        <h2>Competencies</h2>
        <div className="table-wrap">
          <table>
            <tbody>
              {competencies.map((competency) =>
                editing?.id === competency.id ? (
                  <tr key={competency.id}>
                    <td colSpan={3}>
                      <form onSubmit={handleSaveCompetency} className="form-grid">
                        <input
                          aria-label="Name"
                          value={editing.name}
                          onChange={(e) => setEditing({ ...editing, name: e.target.value })}
                          required
                        />
                        <input
                          aria-label="Description"
                          value={editing.description ?? ''}
                          onChange={(e) => setEditing({ ...editing, description: e.target.value })}
                        />
                        <div className="actions">
                          <button type="submit" className="btn-small">
                            Save
                          </button>
                          <button type="button" className="btn-secondary btn-small" onClick={() => setEditing(null)}>
                            Cancel
                          </button>
                        </div>
                      </form>
                    </td>
                  </tr>
                ) : (
                  <tr key={competency.id}>
                    <td>
                      <strong>{competency.name}</strong> <span className="muted small">{competency.code}</span>
                    </td>
                    <td className="muted">{competency.description}</td>
                    <td>
                      <button type="button" className="btn-secondary btn-small" onClick={() => setEditing(competency)}>
                        Edit
                      </button>
                    </td>
                  </tr>
                ),
              )}
            </tbody>
          </table>
        </div>

        <h3>New competency</h3>
        <form onSubmit={handleCreateCompetency}>
          <div className="form-grid">
            <label>
              Code (e.g. TEST)
              <input
                value={newCompetency.code}
                onChange={(e) => setNewCompetency({ ...newCompetency, code: e.target.value })}
                required
              />
            </label>
            <label>
              Name
              <input
                value={newCompetency.name}
                onChange={(e) => setNewCompetency({ ...newCompetency, name: e.target.value })}
                required
              />
            </label>
            <label>
              Description
              <input
                value={newCompetency.description}
                onChange={(e) => setNewCompetency({ ...newCompetency, description: e.target.value })}
              />
            </label>
          </div>
          <button type="submit">Create competency</button>
        </form>
      </section>
    </>
  )
}
