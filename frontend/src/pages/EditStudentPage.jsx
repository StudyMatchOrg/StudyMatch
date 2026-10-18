import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import StudentForm from '../components/StudentForm.jsx'
import { getStudent, updateStudent } from '../services/studentsApi.js'

export default function EditStudentPage() {
  const studentId = Number(useParams().id)
  const navigate = useNavigate()
  const [student, setStudent] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    getStudent(studentId)
      .then((loaded) =>
        setStudent({
          studentNumber: loaded.studentNumber,
          name: loaded.name,
          email: loaded.email,
          degreeId: loaded.degree.id,
          enrolmentYear: loaded.enrolmentYear,
        }),
      )
      .catch((err) => setError(err.message))
  }, [studentId])

  async function handleSave(changes) {
    await updateStudent(studentId, changes)
    navigate(`/students/${studentId}`)
  }

  return (
    <>
      <p>
        <Link to={`/students/${studentId}`}>← Back to student</Link>
      </p>
      <h1>Edit student</h1>
      <section>
        {error && <p className="alert alert-error">{error}</p>}
        {student && <StudentForm initialStudent={student} submitLabel="Save changes" onSubmit={handleSave} />}
      </section>
    </>
  )
}
