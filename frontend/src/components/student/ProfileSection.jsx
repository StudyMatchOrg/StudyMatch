import { Fragment, useEffect, useState } from 'react'
import { getProfile } from '../../services/profileApi.js'

const STATUS = {
  ACADEMIC: { label: 'Grades', badge: 'badge badge-ok' },
  BLENDED: { label: 'Grades + estimate', badge: 'badge badge-accent' },
  INITIAL_ONLY: { label: 'Teacher estimate', badge: 'badge badge-warn' },
  UNKNOWN: { label: 'No evidence', badge: 'badge' },
}

function confidenceLabel(confidence) {
  if (confidence === 0) return null
  if (confidence < 0.4) return 'Low confidence'
  if (confidence < 0.6) return 'Medium confidence'
  return 'High confidence'
}

const format = (value) => value.toFixed(2)
const percent = (value) => `${Math.round(value * 100)}%`

function EvidenceDetails({ entry, rules }) {
  if (entry.status === 'UNKNOWN') {
    return (
      <div className="evidence">
        <p className="muted small">
          No grades or teacher estimate for this competency yet, so the score is unknown (not 0).
        </p>
      </div>
    )
  }
  const weightedSum = entry.evidence.reduce((sum, item) => sum + item.value * item.weight, 0)
  return (
    <div className="evidence">
      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Evidence</th>
              <th className="number">Value</th>
              <th className="number">Weight</th>
              <th className="number">Share</th>
            </tr>
          </thead>
          <tbody>
            {entry.evidence.map((item) => (
              <tr key={item.label}>
                <td>
                  {item.label}
                  {item.type === 'TEACHER_ASSESSMENT' && <span className="badge badge-warn"> estimate</span>}
                </td>
                <td className="number">{format(item.value)}</td>
                <td className="number">{format(item.weight)}</td>
                <td className="number">{percent(item.share)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <p className="small muted">
        Score = Σ value × weight / Σ weight = {format(weightedSum)} / {format(entry.totalWeight)} ={' '}
        <strong>{format(entry.score)}</strong>. Confidence = W / (W + {rules.confidenceConstant}) ={' '}
        {format(entry.totalWeight)} / {format(entry.totalWeight + rules.confidenceConstant)} ={' '}
        <strong>{format(entry.confidence)}</strong>.
      </p>
    </div>
  )
}

export default function ProfileSection({ studentId, refreshKey }) {
  const [profile, setProfile] = useState(null)
  const [expanded, setExpanded] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    getProfile(studentId)
      .then((loaded) => {
        setProfile(loaded)
        setError(null)
      })
      .catch((err) => setError(err.message))
  }, [studentId, refreshKey])

  if (error) {
    return (
      <section>
        <h2>Competency profile</h2>
        <p className="alert alert-error">{error}</p>
      </section>
    )
  }
  if (!profile) {
    return (
      <section>
        <h2>Competency profile</h2>
        <p className="muted">Calculating profile…</p>
      </section>
    )
  }

  const { rules } = profile
  return (
    <section>
      <h2>Competency profile</h2>
      <p className="muted small">
        Calculated from the best grade of each course unit (weight = competency weight × ECTS / {rules.standardEcts})
        and the teacher&apos;s initial estimate (weight {rules.teacherAssessmentWeight}). Click a competency to see the
        evidence behind it.
      </p>
      <ul className="profile-list">
        {profile.competencies.map((entry) => {
          const id = entry.competency.id
          const isOpen = expanded === id
          const status = STATUS[entry.status]
          const confidence = confidenceLabel(entry.confidence)
          return (
            <Fragment key={id}>
              <li>
                <button
                  type="button"
                  className="profile-row"
                  aria-expanded={isOpen}
                  aria-label={`${entry.competency.name}: ${entry.score === null ? 'unknown' : format(entry.score)}, ${status.label}`}
                  onClick={() => setExpanded(isOpen ? null : id)}
                >
                  <span>
                    <strong>{entry.competency.name}</strong>
                    <br />
                    <span className={status.badge}>{status.label}</span>{' '}
                    {confidence && (
                      <span className="muted small" title={`Confidence ${format(entry.confidence)}`}>
                        {confidence}
                      </span>
                    )}
                  </span>
                  <span className="score-track" aria-hidden="true">
                    {entry.score !== null && (
                      <span
                        className={`score-fill${entry.status === 'INITIAL_ONLY' ? ' estimated' : ''}`}
                        style={{ display: 'block', width: percent(entry.score) }}
                      />
                    )}
                  </span>
                  <span className="score-value">{entry.score === null ? '?' : format(entry.score)}</span>
                </button>
              </li>
              {isOpen && (
                <li>
                  <EvidenceDetails entry={entry} rules={rules} />
                </li>
              )}
            </Fragment>
          )
        })}
      </ul>
      <div className="legend">
        <span>
          <span className="legend-swatch score-fill" />
          based on grades
        </span>
        <span>
          <span className="legend-swatch score-fill estimated" />
          teacher estimate only
        </span>
        <span>? unknown: no evidence yet</span>
      </div>
    </section>
  )
}
