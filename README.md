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
| [Domain model](docs/initial-domain-model/domain-model.md) | Domain concepts, relationships and constraints. |
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

These are local development defaults. To use different settings, set the environment
variables `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` before starting the backend.
The tables are created automatically when the backend starts.

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
