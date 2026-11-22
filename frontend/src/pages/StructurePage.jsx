import { useCallback, useEffect, useState } from 'react'
import { createCourseUnit, createDegree, getCourseUnits, getDegrees } from '../services/academicApi.js'

const EMPTY_DEGREE = { code: '', name: '' }
const EMPTY_COURSE_UNIT = { code: '', name: '', ects: 6, curricularYear: 1, semester: 1, degreeIds: [] }

// Admin only: degrees and course units (UC3 to UC6).
export default function StructurePage() {
  const [degrees, setDegrees] = useState([])
  const [courseUnits, setCourseUnits] = useState([])
  const [degree, setDegree] = useState(EMPTY_DEGREE)
  const [courseUnit, setCourseUnit] = useState(EMPTY_COURSE_UNIT)
  const [message, setMessage] = useState(null)
  const [error, setError] = useState(null)

  const load = useCallback(
    () =>
      Promise.all([getDegrees(), getCourseUnits()])
        .then(([loadedDegrees, units]) => {
          setDegrees(loadedDegrees)
          setCourseUnits(units)
        })
        .catch((err) => setError(err.message)),
    [],
  )

  useEffect(() => {
    load()
  }, [load])

  async function save(action, success, reset) {
    setError(null)
    setMessage(null)
    try {
      await action()
      setMessage(success)
      reset()
      await load()
    } catch (err) {
      setError(err.message)
    }
  }

  function handleCreateDegree(event) {
    event.preventDefault()
    save(() => createDegree(degree), `Degree ${degree.code.toUpperCase()} created.`, () => setDegree(EMPTY_DEGREE))
  }

  function handleCreateCourseUnit(event) {
    event.preventDefault()
    const body = {
      ...courseUnit,
      ects: Number(courseUnit.ects),
      curricularYear: Number(courseUnit.curricularYear),
      semester: Number(courseUnit.semester),
    }
    save(
      () => createCourseUnit(body),
      `Course unit ${courseUnit.code.toUpperCase()} created.`,
      () => setCourseUnit(EMPTY_COURSE_UNIT),
    )
  }

  function toggleDegree(degreeId) {
    const ids = courseUnit.degreeIds.includes(degreeId)
      ? courseUnit.degreeIds.filter((id) => id !== degreeId)
      : [...courseUnit.degreeIds, degreeId]
    setCourseUnit({ ...courseUnit, degreeIds: ids })
  }

  function handleCourseUnitChange(event) {
    setCourseUnit({ ...courseUnit, [event.target.name]: event.target.value })
  }

  return (
    <>
      <h1>Degrees and course units</h1>
      {message && <p className="alert alert-success">{message}</p>}
      {error && <p className="alert alert-error">{error}</p>}

      <section>
        <h2>Degrees</h2>
        <div className="table-wrap">
          <table>
            <tbody>
              {degrees.map((d) => (
                <tr key={d.id}>
                  <td>
                    <strong>{d.code}</strong>
                  </td>
                  <td>{d.name}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <h3>New degree</h3>
        <form onSubmit={handleCreateDegree}>
          <div className="form-grid">
            <label>
              Code
              <input value={degree.code} onChange={(e) => setDegree({ ...degree, code: e.target.value })} required />
            </label>
            <label>
              Name
              <input value={degree.name} onChange={(e) => setDegree({ ...degree, name: e.target.value })} required />
            </label>
          </div>
          <button type="submit">Create degree</button>
        </form>
      </section>

      <section>
        <h2>Course units</h2>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Code</th>
                <th>Name</th>
                <th className="number">ECTS</th>
                <th className="number">Year</th>
                <th className="number">Semester</th>
                <th>Degrees</th>
              </tr>
            </thead>
            <tbody>
              {courseUnits.map((u) => (
                <tr key={u.id}>
                  <td>
                    <strong>{u.code}</strong>
                  </td>
                  <td>{u.name}</td>
                  <td className="number">{u.ects}</td>
                  <td className="number">{u.curricularYear}</td>
                  <td className="number">{u.semester}</td>
                  <td>{u.degreeCodes.join(', ')}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <h3>New course unit</h3>
        <form onSubmit={handleCreateCourseUnit}>
          <div className="form-grid">
            <label>
              Code
              <input name="code" value={courseUnit.code} onChange={handleCourseUnitChange} required />
            </label>
            <label>
              Name
              <input name="name" value={courseUnit.name} onChange={handleCourseUnitChange} required />
            </label>
            <label>
              ECTS
              <input name="ects" type="number" min="0.5" step="0.5" value={courseUnit.ects} onChange={handleCourseUnitChange} required />
            </label>
            <label>
              Curricular year
              <input
                name="curricularYear"
                type="number"
                min="1"
                value={courseUnit.curricularYear}
                onChange={handleCourseUnitChange}
                required
              />
            </label>
            <label>
              Semester
              <select name="semester" value={courseUnit.semester} onChange={handleCourseUnitChange}>
                <option value="1">1</option>
                <option value="2">2</option>
              </select>
            </label>
          </div>
          <div className="chips">
            <span className="small muted">Degrees:</span>
            {degrees.map((d) => (
              <label key={d.id} className="inline">
                <input type="checkbox" checked={courseUnit.degreeIds.includes(d.id)} onChange={() => toggleDegree(d.id)} />
                {d.code}
              </label>
            ))}
          </div>
          <button type="submit">Create course unit</button>
        </form>
      </section>
    </>
  )
}
