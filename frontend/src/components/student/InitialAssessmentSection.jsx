import { useCallback, useEffect, useState } from 'react'
import { getAssessments, saveAssessments } from '../../services/assessmentsApi.js'
import { getCompetencies } from '../../services/competenciesApi.js'

// Competency id -> value as typed in the form ('' means not assessed).
function toFormValues(assessments) {
  const values = {}
  for (const assessment of assessments) {
    values[assessment.competency.id] = String(assessment.value)
  }
  return values
}

export default function InitialAssessmentSection({ studentId, onChange }) {
  const [competencies, setCompetencies] = useState([])
  const [assessments, setAssessments] = useState([])
  const [values, setValues] = useState({})
  const [error, setError] = useState(null)
  const [saved, setSaved] = useState(false)

  const loadAssessments = useCallback(
    () =>
      getAssessments(studentId)
        .then((loaded) => {
          setAssessments(loaded)
          setValues(toFormValues(loaded))
        })
        .catch((err) => setError(err.message)),
    [studentId],
  )

  useEffect(() => {
    loadAssessments()
    getCompetencies()
      .then(setCompetencies)
      .catch((err) => setError(err.message))
  }, [loadAssessments])

  function handleChange(competencyId, value) {
    setSaved(false)
    setValues({ ...values, [competencyId]: value })
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    const entries = competencies.map((competency) => {
      const value = values[competency.id]
      return { competencyId: competency.id, value: value === undefined || value === '' ? null : Number(value) }
    })
    try {
      await saveAssessments(studentId, entries)
      await loadAssessments()
      setSaved(true)
      onChange()
    } catch (err) {
      setError(err.message)
    }
  }

  const dates = Object.fromEntries(assessments.map((assessment) => [assessment.competency.id, assessment.assessedOn]))

  return (
    <section>
      <h2>Initial assessment</h2>
      <p className="muted small">
        For students with little or no academic history (cold start), the teacher can estimate each competency from 0
        to 1. An estimate counts as much as one 6-ECTS course unit, so it fades as grades arrive. Leave a field empty if
        you have not observed that competency.
      </p>
      <form onSubmit={handleSubmit}>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Competency</th>
                <th>Estimate (0–1)</th>
                <th>Assessed on</th>
              </tr>
            </thead>
            <tbody>
              {competencies.map((competency) => (
                <tr key={competency.id}>
                  <td title={competency.description}>{competency.name}</td>
                  <td>
                    <input
                      type="number"
                      step="0.05"
                      aria-label={`${competency.name} estimate`}
                      value={values[competency.id] ?? ''}
                      onChange={(event) => handleChange(competency.id, event.target.value)}
                    />
                  </td>
                  <td className="muted small">{dates[competency.id] ?? '–'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="actions">
          <button type="submit">Save assessment</button>
          {saved && <span className="ok small">Saved</span>}
        </div>
        {error && <p className="alert alert-error">{error}</p>}
      </form>
    </section>
  )
}
