import { useEffect, useState } from 'react'
import HealthStatus from '../components/HealthStatus.jsx'
import StudentForm from '../components/StudentForm.jsx'
import StudentList from '../components/StudentList.jsx'
import { createStudent, getStudents } from '../services/api.js'

export default function StudentsPage() {
  const [students, setStudents] = useState([])
  const [error, setError] = useState(null)

  function loadStudents() {
    return getStudents()
      .then(setStudents)
      .catch((err) => setError(err.message))
  }

  useEffect(() => {
    loadStudents()
  }, [])

  async function handleCreate(student) {
    await createStudent(student)
    await loadStudents()
  }

  return (
    <main>
      <h1>StudyMatch</h1>
      <HealthStatus />

      <section>
        <h2>Add student</h2>
        <StudentForm onSubmit={handleCreate} />
      </section>

      <section>
        <h2>Students</h2>
        {error ? <p className="error">{error}</p> : <StudentList students={students} />}
      </section>
    </main>
  )
}
