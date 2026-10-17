import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getStudent } from '../../services/studentsApi.js'

export default function StudentHeader({ studentId }) {
  const [student, setStudent] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    getStudent(studentId)
      .then(setStudent)
      .catch((err) => setError(err.message))
  }, [studentId])

  if (error) {
    return <p className="alert alert-error">{error}</p>
  }
  if (!student) {
    return <p className="muted">Loading student…</p>
  }
  return (
    <div className="page-header">
      <div>
        <h1>{student.name}</h1>
        <p className="muted small">
          {student.studentNumber} · {student.degree.name} · enrolled in {student.enrolmentYear} · {student.email}
        </p>
      </div>
      <Link className="btn btn-secondary" to={`/students/${student.id}/edit`}>
        Edit student
      </Link>
    </div>
  )
}
