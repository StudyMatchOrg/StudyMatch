import { useState } from 'react'

const EMPTY_STUDENT = { studentNumber: '', name: '', email: '' }

export default function StudentForm({ onSubmit }) {
  const [student, setStudent] = useState(EMPTY_STUDENT)
  const [error, setError] = useState(null)

  function handleChange(event) {
    setStudent({ ...student, [event.target.name]: event.target.value })
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    try {
      await onSubmit(student)
      setStudent(EMPTY_STUDENT)
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <input name="studentNumber" placeholder="Student number" value={student.studentNumber} onChange={handleChange} required />
      <input name="name" placeholder="Name" value={student.name} onChange={handleChange} required />
      <input name="email" type="email" placeholder="Email" value={student.email} onChange={handleChange} required />
      <button type="submit">Add student</button>
      {error && <p className="error">{error}</p>}
    </form>
  )
}
