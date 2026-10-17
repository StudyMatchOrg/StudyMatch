import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import AcademicHistorySection from '../components/student/AcademicHistorySection.jsx'
import InitialAssessmentSection from '../components/student/InitialAssessmentSection.jsx'
import ProfileSection from '../components/student/ProfileSection.jsx'
import StudentHeader from '../components/student/StudentHeader.jsx'

// Each section is owned by a different feature. The page only places them and tells the
// profile to recalculate when grades or initial assessments change.
export default function StudentDetailPage() {
  const studentId = Number(useParams().id)
  const [profileVersion, setProfileVersion] = useState(0)
  const refreshProfile = () => setProfileVersion((version) => version + 1)

  return (
    <>
      <p>
        <Link to="/students">← All students</Link>
      </p>
      <StudentHeader studentId={studentId} />
      <div className="student-grid">
        <div className="stack">
          <AcademicHistorySection studentId={studentId} onChange={refreshProfile} />
          <InitialAssessmentSection studentId={studentId} onChange={refreshProfile} />
        </div>
        <ProfileSection studentId={studentId} refreshKey={profileVersion} />
      </div>
    </>
  )
}
