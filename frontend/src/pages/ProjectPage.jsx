import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import GroupGenerationSection from '../components/project/GroupGenerationSection.jsx'
import { getProject } from '../services/projectsApi.js'

export default function ProjectPage() {
  const { id } = useParams()
  const [project, setProject] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    getProject(id)
      .then(setProject)
      .catch((err) => setError(err.message))
  }, [id])

  if (error) {
    return <p className="alert alert-error">{error}</p>
  }
  if (!project) {
    return <p className="muted">Loading…</p>
  }

  return (
    <>
      <p>
        <Link to="/projects">← All projects</Link>
      </p>
      <div className="page-header">
        <h1>{project.name}</h1>
        <Link className="btn btn-secondary" to={`/projects/${project.id}/edit`}>
          Edit project
        </Link>
      </div>
      {project.description && <p>{project.description}</p>}

      <section>
        <div className="stat-grid">
          <div className="stat">
            <span className="stat-value">{project.students.length}</span>
            <span className="stat-label">students</span>
          </div>
          <div className="stat">
            <span className="stat-value">{project.groupSize}</span>
            <span className="stat-label">students per group (chosen size)</span>
          </div>
          <div className="stat">
            <span className="stat-value">{project.plannedSizes.length || '–'}</span>
            <span className="stat-label">
              groups{project.plannedSizes.length > 0 && `: ${project.plannedSizes.join(' + ')}`}
            </span>
          </div>
        </div>
        {project.warnings.map((warning) => (
          <p key={warning} className="alert alert-warn">
            {warning}
          </p>
        ))}
        <p className="muted small">
          {project.courseUnit ? `${project.courseUnit.code} – ${project.courseUnit.name}` : 'No course unit'}
          {project.academicYear && ` · ${project.academicYear}`} · Teacher: {project.ownerName}
        </p>
        <h3>Relevant competencies</h3>
        <div className="chips">
          {project.competencies.map((c) => (
            <span key={c.competency.id} className="badge badge-accent">
              {c.competency.name} · {c.importance}
            </span>
          ))}
        </div>
      </section>

      <GroupGenerationSection project={project} />

      <section>
        <h2>Students ({project.students.length})</h2>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Number</th>
                <th>Name</th>
                <th>Degree</th>
              </tr>
            </thead>
            <tbody>
              {project.students.map((student) => (
                <tr key={student.id}>
                  <td>{student.studentNumber}</td>
                  <td>
                    <Link to={`/students/${student.id}`}>{student.name}</Link>
                  </td>
                  <td>{student.degree.code}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </>
  )
}
