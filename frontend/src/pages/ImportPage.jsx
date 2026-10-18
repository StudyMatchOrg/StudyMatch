import { useState } from 'react'
import { Link } from 'react-router-dom'
import { importAcademicRecords } from '../services/importApi.js'

const COLUMNS = [
  ['studentNumber', 'Number of an existing student', '52031'],
  ['courseUnitCode', 'Course unit of the student’s degree', 'POO'],
  ['academicYear', 'Two consecutive years', '2025/2026'],
  ['semester', '1 or 2', '2'],
  ['status', 'PASSED, FAILED or ENROLLED', 'PASSED'],
  ['grade', '0–20; empty when ENROLLED', '15'],
]

export default function ImportPage() {
  const [file, setFile] = useState(null)
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)
  const [importing, setImporting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    setResult(null)
    setImporting(true)
    try {
      setResult(await importAcademicRecords(file))
    } catch (err) {
      setError(err.message)
    } finally {
      setImporting(false)
    }
  }

  return (
    <>
      <h1>Import academic history</h1>

      <section>
        <h2>File format</h2>
        <p>
          A CSV file with a header line and one academic record per line. Columns may be separated by commas or
          semicolons, in any order. Each line follows the same rules as the academic history form: invalid lines are
          reported and skipped, the valid ones are saved.
        </p>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Column</th>
                <th>Meaning</th>
                <th>Example</th>
              </tr>
            </thead>
            <tbody>
              {COLUMNS.map(([name, meaning, example]) => (
                <tr key={name}>
                  <td>
                    <code>{name}</code>
                  </td>
                  <td>{meaning}</td>
                  <td className="muted">{example}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <p>
          <a href="/samples/academic-records-sample.csv" download>
            Download a sample file
          </a>{' '}
          <span className="muted small">(it contains some invalid lines on purpose)</span>
        </p>
      </section>

      <section>
        <h2>Upload</h2>
        <form onSubmit={handleSubmit}>
          <input type="file" accept=".csv,text/csv" onChange={(event) => setFile(event.target.files[0] ?? null)} required />
          <button type="submit" disabled={!file || importing}>
            {importing ? 'Importing…' : 'Import'}
          </button>
        </form>
        {error && <p className="alert alert-error">{error}</p>}
      </section>

      {result && (
        <section>
          <h2>Result</h2>
          <p className={result.imported > 0 ? 'alert alert-success' : 'alert alert-error'}>
            {result.imported} of {result.totalLines} lines imported.
          </p>
          {result.rejected.length > 0 && (
            <>
              <h3>Rejected lines</h3>
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th className="number">Line</th>
                      <th>Reason</th>
                    </tr>
                  </thead>
                  <tbody>
                    {result.rejected.map((line) => (
                      <tr key={line.line}>
                        <td className="number">{line.line}</td>
                        <td>{line.message}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}
          <p>
            <Link to="/students">Go to the students</Link> to see the updated profiles.
          </p>
        </section>
      )}
    </>
  )
}
