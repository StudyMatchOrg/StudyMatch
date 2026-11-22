import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { getCourseUnits } from '../services/academicApi.js'
import { getCompetencies } from '../services/competenciesApi.js'
import { createProject, getCandidates, getProject, updateProject } from '../services/projectsApi.js'
import { getStudents } from '../services/studentsApi.js'

// The academic year running now, e.g. 2026/2027 from September 2026 to August 2027.
function currentAcademicYear() {
  const today = new Date()
  const start = today.getMonth() >= 8 ? today.getFullYear() : today.getFullYear() - 1
  return `${start}/${start + 1}`
}

const NEW_PROJECT = {
  name: '',
  description: '',
  courseUnitId: '',
  academicYear: currentAcademicYear(),
  groupSize: 5,
}

// Creates a project (no id in the URL) or edits one.
export default function ProjectFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [project, setProject] = useState(NEW_PROJECT)
  const [selected, setSelected] = useState(new Set())
  const [importance, setImportance] = useState({})
  const [courseUnits, setCourseUnits] = useState([])
  const [competencies, setCompetencies] = useState([])
  const [students, setStudents] = useState([])
  const [studentFilter, setStudentFilter] = useState('')
  const [message, setMessage] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    Promise.all([getCourseUnits(), getCompetencies(), getStudents(), id ? getProject(id) : null])
      .then(([units, loadedCompetencies, loadedStudents, existing]) => {
        setCourseUnits(units)
        setCompetencies(loadedCompetencies)
        setStudents(loadedStudents)
        if (existing) {
          setProject({
            name: existing.name,
            description: existing.description ?? '',
            courseUnitId: existing.courseUnit?.id ?? '',
            academicYear: existing.academicYear ?? '',
            groupSize: existing.groupSize,
          })
          setSelected(new Set(existing.students.map((s) => s.id)))
          setImportance(Object.fromEntries(existing.competencies.map((c) => [c.competency.id, String(c.importance)])))
        } else {
          // By default every competency matters equally; the teacher adjusts or removes them.
          setImportance(Object.fromEntries(loadedCompetencies.map((c) => [c.id, '1'])))
        }
      })
      .catch((err) => setError(err.message))
  }, [id])

  function handleChange(event) {
    setProject({ ...project, [event.target.name]: event.target.value })
  }

  function toggleStudent(studentId) {
    const next = new Set(selected)
    if (next.has(studentId)) {
      next.delete(studentId)
    } else {
      next.add(studentId)
    }
    setSelected(next)
  }

  function toggleCompetency(competencyId) {
    const next = { ...importance }
    if (next[competencyId] === undefined) {
      next[competencyId] = '1'
    } else {
      delete next[competencyId]
    }
    setImportance(next)
  }

  async function loadEnrolled() {
    setError(null)
    setMessage(null)
    try {
      const enrolled = await getCandidates(project.courseUnitId, project.academicYear)
      setSelected(new Set(enrolled.map((s) => s.id)))
      setMessage(`${enrolled.length} students are enrolled in this course unit in ${project.academicYear}.`)
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    const body = {
      name: project.name,
      description: project.description,
      courseUnitId: project.courseUnitId === '' ? null : Number(project.courseUnitId),
      academicYear: project.academicYear,
      groupSize: Number(project.groupSize),
      studentIds: [...selected],
      competencies: Object.entries(importance).map(([competencyId, value]) => ({
        competencyId: Number(competencyId),
        importance: Number(value),
      })),
    }
    try {
      const saved = id ? await updateProject(id, body) : await createProject(body)
      navigate(`/projects/${saved.id}`)
    } catch (err) {
      setError(err.message)
    }
  }

  const query = studentFilter.trim().toLowerCase()
  const visibleStudents = students.filter(
    (s) => !query || s.name.toLowerCase().includes(query) || s.studentNumber.includes(query),
  )

  return (
    <>
      <p>
        <Link to={id ? `/projects/${id}` : '/projects'}>← Back</Link>
      </p>
      <h1>{id ? 'Edit project' : 'New project'}</h1>

      <form onSubmit={handleSubmit}>
        <section>
          <h2>Project</h2>
          <div className="form-grid">
            <label>
              Name
              <input name="name" value={project.name} onChange={handleChange} required />
            </label>
            <label>
              Course unit (optional)
              <select name="courseUnitId" value={project.courseUnitId} onChange={handleChange}>
                <option value="">None</option>
                {courseUnits.map((unit) => (
                  <option key={unit.id} value={unit.id}>
                    {unit.code} – {unit.name}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Academic year
              <input name="academicYear" value={project.academicYear} onChange={handleChange} placeholder="2026/2027" />
            </label>
            <label>
              Group size (2 to 10)
              <input name="groupSize" type="number" min="2" max="10" value={project.groupSize} onChange={handleChange} required />
            </label>
          </div>
          <label>
            Description (optional)
            <textarea name="description" rows="2" value={project.description} onChange={handleChange} />
          </label>
          <p className="muted small">
            Groups have this size when possible. When the students do not divide evenly, some groups have one student
            fewer (e.g. 23 students in groups of 5 give 5 + 5 + 5 + 4 + 4).
          </p>
        </section>

        <section>
          <h2>Students</h2>
          <div className="actions">
            <button
              type="button"
              className="btn-secondary"
              disabled={!project.courseUnitId || !project.academicYear}
              onClick={loadEnrolled}
            >
              Load students enrolled in the course unit
            </button>
            <button type="button" className="btn-secondary" onClick={() => setSelected(new Set())}>
              Clear
            </button>
            <input
              placeholder="Find a student"
              value={studentFilter}
              onChange={(e) => setStudentFilter(e.target.value)}
            />
            <strong>{selected.size} selected</strong>
          </div>
          {message && <p className="alert alert-success">{message}</p>}
          <div className="check-list">
            {visibleStudents.map((student) => (
              <label key={student.id} className="inline">
                <input type="checkbox" checked={selected.has(student.id)} onChange={() => toggleStudent(student.id)} />
                {student.name} <span className="muted small">{student.studentNumber}</span>
              </label>
            ))}
          </div>
        </section>

        <section>
          <h2>Relevant competencies</h2>
          <p className="muted small">
            Groups are balanced on these competencies. Importance goes from 0.1 (minor) to 1 (essential).
          </p>
          <div className="stack">
            {competencies.map((competency) => (
              <div key={competency.id} className="importance-row">
                <label className="inline">
                  <input
                    type="checkbox"
                    checked={importance[competency.id] !== undefined}
                    onChange={() => toggleCompetency(competency.id)}
                  />
                  {competency.name}
                </label>
                {importance[competency.id] !== undefined && (
                  <input
                    type="number"
                    min="0.1"
                    max="1"
                    step="0.1"
                    aria-label={`Importance of ${competency.name}`}
                    value={importance[competency.id]}
                    onChange={(e) => setImportance({ ...importance, [competency.id]: e.target.value })}
                    required
                  />
                )}
              </div>
            ))}
          </div>
        </section>

        <div className="actions">
          <button type="submit">{id ? 'Save project' : 'Create project'}</button>
          {error && <p className="alert alert-error">{error}</p>}
        </div>
      </form>
    </>
  )
}
