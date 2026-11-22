import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getProjects } from '../services/projectsApi.js'

export default function ProjectsPage() {
  const [projects, setProjects] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    getProjects()
      .then(setProjects)
      .catch((err) => setError(err.message))
  }, [])

  return (
    <>
      <div className="page-header">
        <h1>Projects</h1>
        <Link className="btn" to="/projects/new">
          New project
        </Link>
      </div>
      <p className="muted">
        A project is where groups are formed: which students take part, the group size and the competencies that
        matter.
      </p>

      {error && <p className="alert alert-error">{error}</p>}

      <section>
        {projects?.length === 0 && <p className="muted">No projects yet. Create one to form groups.</p>}
        {projects?.length > 0 && (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Project</th>
                  <th>Course unit</th>
                  <th>Year</th>
                  <th className="number">Group size</th>
                  <th className="number">Students</th>
                  <th>Teacher</th>
                  <th>Created</th>
                </tr>
              </thead>
              <tbody>
                {projects.map((project) => (
                  <tr key={project.id}>
                    <td>
                      <Link to={`/projects/${project.id}`}>{project.name}</Link>
                    </td>
                    <td>{project.courseUnitCode ?? <span className="muted">–</span>}</td>
                    <td>{project.academicYear ?? <span className="muted">–</span>}</td>
                    <td className="number">{project.groupSize}</td>
                    <td className="number">{project.studentCount}</td>
                    <td>{project.ownerName}</td>
                    <td>{new Date(project.createdAt).toLocaleDateString('en-GB')}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </>
  )
}
