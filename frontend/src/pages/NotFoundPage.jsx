import { Link } from 'react-router-dom'

export default function NotFoundPage() {
  return (
    <section>
      <h1>Page not found</h1>
      <p>
        <Link to="/students">Go to the students list</Link>
      </p>
    </section>
  )
}
