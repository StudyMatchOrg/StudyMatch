import { useEffect, useState } from 'react'
import { getDegrees } from '../services/academicApi.js'

const EMPTY_STUDENT = { studentNumber: '', name: '', email: '', degreeId: '', enrolmentYear: new Date().getFullYear() }

// Used both to add a student (no initialStudent) and to edit one.
export default function StudentForm({ initialStudent, submitLabel = 'Add student', onSubmit }) {
  const [student, setStudent] = useState(initialStudent ?? EMPTY_STUDENT)
  const [degrees, setDegrees] = useState([])
  const [error, setError] = useState(null)

  useEffect(() => {
    getDegrees()
      .then(setDegrees)
      .catch((err) => setError(err.message))
  }, [])

  function handleChange(event) {
    setStudent({ ...student, [event.target.name]: event.target.value })
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    try {
      await onSubmit({
        ...student,
        degreeId: Number(student.degreeId),
        enrolmentYear: Number(student.enrolmentYear),
      })
      if (!initialStudent) {
        setStudent(EMPTY_STUDENT)
      }
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="form-grid">
        <label>
          Student number
          <input name="studentNumber" value={student.studentNumber} onChange={handleChange} required />
        </label>
        <label>
          Name
          <input name="name" value={student.name} onChange={handleChange} required />
        </label>
        <label>
          Email
          <input name="email" type="email" value={student.email} onChange={handleChange} required />
        </label>
        <label>
          Degree
          <select name="degreeId" value={student.degreeId} onChange={handleChange} required>
            <option value="">Choose a degree</option>
            {degrees.map((degree) => (
              <option key={degree.id} value={degree.id}>
                {degree.code} – {degree.name}
              </option>
            ))}
          </select>
        </label>
        <label>
          Enrolment year
          <input name="enrolmentYear" type="number" min="2000" value={student.enrolmentYear} onChange={handleChange} required />
        </label>
      </div>
      <button type="submit">{submitLabel}</button>
      {error && <p className="alert alert-error">{error}</p>}
    </form>
  )
}
