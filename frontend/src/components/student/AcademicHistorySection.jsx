import { useCallback, useEffect, useState } from 'react'
import { getCourseUnits } from '../../services/academicApi.js'
import { addRecord, changeRecord, deleteRecord, getRecords } from '../../services/recordsApi.js'
import { getStudent } from '../../services/studentsApi.js'

const STATUS_BADGE = { PASSED: 'badge badge-ok', FAILED: 'badge badge-error', ENROLLED: 'badge badge-accent' }
const STATUS_LABEL = { PASSED: 'Passed', FAILED: 'Failed', ENROLLED: 'Enrolled' }

// Academic years start in September: in October 2026 the current year is 2026/2027.
function currentAcademicYear() {
  const today = new Date()
  const start = today.getMonth() >= 8 ? today.getFullYear() : today.getFullYear() - 1
  return `${start}/${start + 1}`
}

function emptyForm() {
  return { id: null, courseUnitId: '', academicYear: currentAcademicYear(), semester: '1', status: 'PASSED', grade: '' }
}

export default function AcademicHistorySection({ studentId, onChange }) {
  const [records, setRecords] = useState([])
  const [courseUnits, setCourseUnits] = useState([])
  const [form, setForm] = useState(emptyForm)
  const [error, setError] = useState(null)

  const loadRecords = useCallback(
    () =>
      getRecords(studentId)
        .then(setRecords)
        .catch((err) => setError(err.message)),
    [studentId],
  )

  useEffect(() => {
    loadRecords()
    getStudent(studentId)
      .then((student) => getCourseUnits(student.degree.id))
      .then(setCourseUnits)
      .catch((err) => setError(err.message))
  }, [studentId, loadRecords])

  function handleChange(event) {
    const { name, value } = event.target
    const next = { ...form, [name]: value }
    if (name === 'status' && value === 'ENROLLED') {
      next.grade = ''
    }
    if (name === 'courseUnitId') {
      const unit = courseUnits.find((candidate) => String(candidate.id) === value)
      if (unit) {
        next.semester = String(unit.semester)
      }
    }
    setForm(next)
  }

  async function afterChange() {
    setForm(emptyForm())
    await loadRecords()
    onChange()
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    const data = {
      academicYear: form.academicYear,
      semester: Number(form.semester),
      status: form.status,
      grade: form.status === 'ENROLLED' || form.grade === '' ? null : Number(form.grade),
    }
    try {
      if (form.id) {
        await changeRecord(form.id, data)
      } else {
        await addRecord(studentId, { ...data, courseUnitId: Number(form.courseUnitId) })
      }
      await afterChange()
    } catch (err) {
      setError(err.message)
    }
  }

  function startEdit(record) {
    setError(null)
    setForm({
      id: record.id,
      courseUnitId: String(record.courseUnit.id),
      academicYear: record.academicYear,
      semester: String(record.semester),
      status: record.status,
      grade: record.grade ?? '',
    })
  }

  async function handleDelete(record) {
    if (!window.confirm(`Delete the ${record.courseUnit.code} record of ${record.academicYear}?`)) {
      return
    }
    setError(null)
    try {
      await deleteRecord(record.id)
      await afterChange()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <section>
      <h2>Academic history</h2>
      {records.length === 0 ? (
        <p className="muted">No academic records yet.</p>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Year</th>
                <th>Course unit</th>
                <th>Status</th>
                <th className="number">Grade</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {records.map((record) => (
                <tr key={record.id}>
                  <td className="small">
                    {record.academicYear} S{record.semester}
                  </td>
                  <td>
                    <strong>{record.courseUnit.code}</strong>{' '}
                    <span className="muted small">{record.courseUnit.name}</span>
                  </td>
                  <td>
                    <span className={STATUS_BADGE[record.status]}>{STATUS_LABEL[record.status]}</span>
                  </td>
                  <td className="number">{record.grade ?? '–'}</td>
                  <td>
                    <div className="actions">
                      <button type="button" className="btn-small btn-secondary" onClick={() => startEdit(record)}>
                        Edit
                      </button>
                      <button type="button" className="btn-small btn-danger" onClick={() => handleDelete(record)}>
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <h3 style={{ marginTop: 16 }}>{form.id ? 'Edit record' : 'Add record'}</h3>
      <form onSubmit={handleSubmit}>
        <div className="form-grid">
          <label>
            Course unit
            <select name="courseUnitId" value={form.courseUnitId} onChange={handleChange} required disabled={Boolean(form.id)}>
              <option value="">Choose a course unit</option>
              {courseUnits.map((unit) => (
                <option key={unit.id} value={unit.id}>
                  {unit.code} – {unit.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Academic year
            <input name="academicYear" value={form.academicYear} onChange={handleChange} placeholder="2025/2026" required />
          </label>
          <label>
            Semester
            <select name="semester" value={form.semester} onChange={handleChange}>
              <option value="1">1</option>
              <option value="2">2</option>
            </select>
          </label>
          <label>
            Status
            <select name="status" value={form.status} onChange={handleChange}>
              <option value="PASSED">Passed</option>
              <option value="FAILED">Failed</option>
              <option value="ENROLLED">Enrolled</option>
            </select>
          </label>
          <label>
            Grade (0–20)
            <input
              name="grade"
              type="number"
              value={form.grade}
              onChange={handleChange}
              disabled={form.status === 'ENROLLED'}
              required={form.status !== 'ENROLLED'}
            />
          </label>
        </div>
        <div className="actions">
          <button type="submit">{form.id ? 'Save changes' : 'Add record'}</button>
          {form.id && (
            <button type="button" className="btn-secondary" onClick={() => setForm(emptyForm())}>
              Cancel
            </button>
          )}
        </div>
        {error && <p className="alert alert-error">{error}</p>}
      </form>
    </section>
  )
}
