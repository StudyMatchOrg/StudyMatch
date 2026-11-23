import { Link } from 'react-router-dom'

// competencyName: when the search filtered or sorted by a competency, its score is shown too.
export default function StudentList({ students, competencyName }) {
  if (students.length === 0) {
    return <p className="muted">No students match. Try removing some filters.</p>
  }
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Number</th>
            <th>Name</th>
            <th>Degree</th>
            <th className="number">Enrolled</th>
            <th>Email</th>
            {competencyName && <th className="number">{competencyName}</th>}
          </tr>
        </thead>
        <tbody>
          {students.map((student) => (
            <tr key={student.id}>
              <td>{student.studentNumber}</td>
              <td>
                <Link to={`/students/${student.id}`}>{student.name}</Link>{' '}
                {student.lowEvidence && <span className="badge badge-warn">no grades yet</span>}
              </td>
              <td>{student.degree.code}</td>
              <td className="number">{student.enrolmentYear}</td>
              <td>{student.email}</td>
              {competencyName && (
                <td className="number">
                  {student.competencyScore == null ? <span className="muted">?</span> : student.competencyScore.toFixed(2)}
                </td>
              )}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
