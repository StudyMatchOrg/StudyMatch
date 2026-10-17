import { Link } from 'react-router-dom'

export default function StudentList({ students }) {
  if (students.length === 0) {
    return <p className="muted">No students registered yet.</p>
  }
  return (
    <table>
      <thead>
        <tr>
          <th>Number</th>
          <th>Name</th>
          <th>Email</th>
        </tr>
      </thead>
      <tbody>
        {students.map((student) => (
          <tr key={student.id}>
            <td>{student.studentNumber}</td>
            <td>
              <Link to={`/students/${student.id}`}>{student.name}</Link>
            </td>
            <td>{student.email}</td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
