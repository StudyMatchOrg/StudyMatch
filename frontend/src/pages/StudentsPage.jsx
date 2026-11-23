import { useCallback, useEffect, useState } from 'react'
import { useAuth } from '../auth/useAuth.js'
import StudentForm from '../components/StudentForm.jsx'
import StudentList from '../components/StudentList.jsx'
import { getCourseUnits, getDegrees } from '../services/academicApi.js'
import { getCompetencies } from '../services/competenciesApi.js'
import { createStudent, getStudents } from '../services/studentsApi.js'

const NO_FILTERS = {
  q: '',
  degreeId: '',
  enrolmentYear: '',
  courseUnitId: '',
  competency: '',
  minScore: '',
  lowEvidence: false,
  sort: 'number',
}

export default function StudentsPage() {
  const { isAdmin } = useAuth()
  const [students, setStudents] = useState([])
  const [filters, setFilters] = useState(NO_FILTERS)
  const [degrees, setDegrees] = useState([])
  const [courseUnits, setCourseUnits] = useState([])
  const [competencies, setCompetencies] = useState([])
  const [error, setError] = useState(null)

  const loadStudents = useCallback(() => {
    setError(null)
    return getStudents({ ...filters, lowEvidence: filters.lowEvidence || '' })
      .then(setStudents)
      .catch((err) => setError(err.message))
  }, [filters])

  useEffect(() => {
    Promise.all([getDegrees(), getCourseUnits(), getCompetencies()])
      .then(([loadedDegrees, units, loadedCompetencies]) => {
        setDegrees(loadedDegrees)
        setCourseUnits(units)
        setCompetencies(loadedCompetencies)
      })
      .catch((err) => setError(err.message))
  }, [])

  // Searches as the filters change, waiting a moment while the user is still typing.
  useEffect(() => {
    const timer = setTimeout(loadStudents, 250)
    return () => clearTimeout(timer)
  }, [loadStudents])

  function handleFilter(event) {
    const { name, value, type, checked } = event.target
    const next = { ...filters, [name]: type === 'checkbox' ? checked : value }
    if (name === 'competency' && value === '') {
      next.minScore = ''
      next.sort = next.sort === 'score' ? 'number' : next.sort
    }
    setFilters(next)
  }

  async function handleCreate(student) {
    await createStudent(student)
    await loadStudents()
  }

  const competencyName = competencies.find((c) => c.code === filters.competency)?.name

  return (
    <>
      <h1>Students</h1>

      {isAdmin && (
        <section>
          <h2>Add student</h2>
          <StudentForm onSubmit={handleCreate} />
        </section>
      )}

      <section>
        <h2>Find students</h2>
        <div className="filters">
          <label>
            Name or number
            <input name="q" value={filters.q} onChange={handleFilter} placeholder="e.g. joao or 5204" />
          </label>
          <label>
            Degree
            <select name="degreeId" value={filters.degreeId} onChange={handleFilter}>
              <option value="">All</option>
              {degrees.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.code}
                </option>
              ))}
            </select>
          </label>
          <label>
            Enrolment year
            <input name="enrolmentYear" type="number" min="2000" value={filters.enrolmentYear} onChange={handleFilter} />
          </label>
          <label>
            Enrolled in
            <select name="courseUnitId" value={filters.courseUnitId} onChange={handleFilter}>
              <option value="">Any course unit</option>
              {courseUnits.map((u) => (
                <option key={u.id} value={u.id}>
                  {u.code} – {u.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Competency
            <select name="competency" value={filters.competency} onChange={handleFilter}>
              <option value="">Any</option>
              {competencies.map((c) => (
                <option key={c.id} value={c.code}>
                  {c.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Minimum score (0 to 1)
            <input
              name="minScore"
              type="number"
              min="0"
              max="1"
              step="0.05"
              value={filters.minScore}
              onChange={handleFilter}
              disabled={!filters.competency}
            />
          </label>
          <label>
            Sort by
            <select name="sort" value={filters.sort} onChange={handleFilter}>
              <option value="number">Number</option>
              <option value="name">Name</option>
              <option value="score" disabled={!filters.competency}>
                Score in the competency
              </option>
            </select>
          </label>
          <label className="inline">
            <input type="checkbox" name="lowEvidence" checked={filters.lowEvidence} onChange={handleFilter} />
            Only students without grades
          </label>
        </div>
        <div className="actions">
          <strong>
            {students.length} {students.length === 1 ? 'student' : 'students'}
          </strong>
          <button type="button" className="btn-secondary btn-small" onClick={() => setFilters(NO_FILTERS)}>
            Clear filters
          </button>
        </div>
      </section>

      <section>
        {error ? <p className="alert alert-error">{error}</p> : <StudentList students={students} competencyName={competencyName} />}
      </section>
    </>
  )
}
