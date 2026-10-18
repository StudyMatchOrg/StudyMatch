import { useEffect, useState } from 'react'
import { getCompetencies, getCourseUnitCompetencies } from '../services/competenciesApi.js'

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
        weights: {},
      })
    }
    rows.get(link.courseUnitId).weights[link.competency.id] = link.weight
  }
  return [...rows.values()]
}

export default function CompetenciesPage() {
  const [competencies, setCompetencies] = useState([])
  const [rows, setRows] = useState([])
  const [error, setError] = useState(null)

  useEffect(() => {
    Promise.all([getCompetencies(), getCourseUnitCompetencies()])
      .then(([loadedCompetencies, links]) => {
        setCompetencies(loadedCompetencies)
        setRows(toRows(links))
      })
      .catch((err) => setError(err.message))
  }, [])

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

      {error && <p className="alert alert-error">{error}</p>}

      <section>
        <h2>Course-unit weights</h2>
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
                      {row.weights[competency.id] ?? <span className="muted">–</span>}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <section>
        <h2>Competency descriptions</h2>
        <div className="table-wrap">
          <table>
            <tbody>
              {competencies.map((competency) => (
                <tr key={competency.id}>
                  <td>
                    <strong>{competency.name}</strong>
                  </td>
                  <td className="muted">{competency.description}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </>
  )
}
