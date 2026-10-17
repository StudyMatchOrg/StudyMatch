import { useEffect, useState } from 'react'
import StudentForm from '../components/StudentForm.jsx'
import StudentList from '../components/StudentList.jsx'
import { createStudent, getStudents } from '../services/studentsApi.js'

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
    <>
      <h1>Students</h1>

      <section>
        <h2>Add student</h2>
        <StudentForm onSubmit={handleCreate} />
      </section>

      <section>
        <h2>All students</h2>
        {error ? <p className="error">{error}</p> : <StudentList students={students} />}
      </section>
    </>
  )
}
