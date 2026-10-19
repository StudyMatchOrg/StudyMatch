# StudyMatch

**Adaptive student group formation.** Group project for the Software Quality course
(Qualidade de Software) at Universidade Portucalense.

## Product context

University students work in groups many times during their degree. These groups are
usually formed randomly, by friendship or by availability, which ignores what each
student has learned and where their strengths are.

StudyMatch builds a profile of each student from their academic history (course units,
grades and the competencies those course units develop) and will use those profiles to
form balanced groups automatically. Two design problems are central to the product:

- **Cold start:** how to represent a student who has little or no academic history yet.
- **Matching:** how to use academic history and competencies to form groups.

## Repository structure

```
StudyMatch/
├── backend/    Spring Boot REST API (Java 21, Maven)
├── frontend/   React application (Vite)
└── docs/       Project documentation
```

## Documentation

| Document | Contents |
|---|---|
| [Technology stack](docs/technology-stack.md) | Chosen technologies and why. |
| [Architecture](docs/architecture/architecture.md) | Components, layers, communication, persistence and main decisions. |
| [Use cases](docs/use-cases.md) | Actors and use-case model, including the team's own proposals. |
| [Domain model](docs/initial-domain-model.md) | Domain concepts, relationships and constraints. |
| [Sprint 2 design](docs/sprint-2-design.md) | Student profile rule, cold-start strategy, CSV import and their justification. |
| [Conventions and workflow](docs/conventions.md) | Coding conventions, Git workflow, Pull Request rules and Definition of Done. |

## Prerequisites

| Tool | Version | Notes |
|---|---|---|
| JDK | 21 | e.g. [Eclipse Temurin](https://adoptium.net/). Set `JAVA_HOME`. |
| Node.js | 22 LTS | Includes npm. |
| MySQL | 8.x | Only needed to run the application. The tests use an in-memory database. |

Maven does not need to be installed: the backend includes the Maven Wrapper (`mvnw`).

## Setup

### 1. Create the database

Connect to MySQL as an administrator and run:

```sql
CREATE DATABASE studymatch;
CREATE USER 'studymatch'@'localhost' IDENTIFIED BY 'studymatch';
GRANT ALL PRIVILEGES ON studymatch.* TO 'studymatch'@'localhost';
```

Then create the file `backend/.env` with the credentials (it is ignored by Git, so your
password is never committed):

```properties
DB_USER=studymatch
DB_PASSWORD=studymatch
```

The same values can be given as environment variables instead. `DB_URL` can also be set
if MySQL does not run on `localhost:3306`. The tables are created automatically when the
backend starts.

> **Upgrading from Sprint 1?** The tables changed (a student now has a degree), so
> recreate the database once: `DROP DATABASE studymatch; CREATE DATABASE studymatch;`

### 2. Run the backend

```bash
cd backend
./mvnw spring-boot:run
```

On Windows (PowerShell or cmd) use `mvnw.cmd` instead of `./mvnw`.
The API is available at <http://localhost:8080/api>. Check it with
<http://localhost:8080/api/health>.

### 3. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

Open <http://localhost:5173>. The development server forwards `/api` requests to the
backend, so the backend must be running.

## Using the application

| Page | What you can do |
|---|---|
| **Students** | List and add students; open a student. |
| **Student page** | Edit the student, manage the academic history, give an initial assessment (cold start) and see the competency profile. Click a competency to see the evidence behind its score. |
| **Competencies** | See the competencies and how much each course unit develops them. |
| **Import** | Import academic history from a CSV file. A sample file is available on the page. |

On startup the backend adds **demo data** if it is missing: degrees, course units,
competencies and four students (Diogo and Ana with grades, Bruno with only a teacher
assessment, Carla with nothing). Changes made in the application are kept. To start with
an empty database, set `DEMO_DATA=false`.

## Build and test

| Command | Run from | What it does |
|---|---|---|
| `./mvnw test` | `backend/` | Runs the unit and integration tests. |
| `./mvnw verify` | `backend/` | Compiles, runs the tests, writes a coverage report to `target/site/jacoco/index.html` and builds the jar in `target/`. |
| `npm run lint` | `frontend/` | Checks the frontend code with oxlint. |
| `npm run build` | `frontend/` | Builds the production frontend into `dist/`. |

GitHub Actions runs the backend build with tests, and the frontend lint and build, on
every Pull Request that changes them.

## Team workflow

Work follows **Issue → Branch → Implementation → Pull Request → Review → Merge**.
Branch naming, Pull Request rules and the Definition of Done are described in
[docs/conventions.md](docs/conventions.md).
