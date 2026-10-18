# Coding Conventions, Workflow and Definition of Done

## Naming
- Java classes: PascalCase (e.g. `StudentService`)
- Methods and variables: camelCase (e.g. `findStudentById`)
- Constants: UPPER_SNAKE_CASE (e.g. `MAX_GROUP_SIZE`)
- React components and their files: PascalCase (e.g. `StudentList.jsx`)
- Code, comments and commit messages are written in English.

## Domain terms (Portuguese → English)
| Use case term | Code name |
|---|---|
| Utilizador | `User` |
| Admin | `Admin` |
| Professor | `Teacher` |
| Estudante / Aluno | `Student` |
| Tipo de Conta | `AccountType` |
| Curso | `Degree` |
| Cadeira | `CourseUnit` |
| Inscrição | `Enrolment` |
| Registo Académico | `AcademicRecord` |
| Competência | `Competency` |
| Avaliação Inicial | `CompetencyAssessment` |
| Perfil do Estudante | `StudentProfile` |
| Evidência | `Evidence` |
| Contexto de Agrupamento | `GroupingContext` |
| Grupo | `Group` |
| Conjunto de Grupos | `GroupSet` |

## Project structure
- Backend packages (root `pt.upt.studymatch`):
    - `controller`: receives the HTTP requests
    - `service`: business logic and rules
    - `repository`: access to the database
    - `domain`: entities (e.g. `Student`, `CourseUnit`) and domain calculations
    - `dto`: objects sent to and received from the frontend
    - `exception`: domain exceptions and the global error handler
- Frontend folders:
    - `components`: reusable parts
    - `pages`: one screen per file
    - `services`: API calls
- Controllers contain no business logic. Business rules live in `service` or `domain`.
- Frontend code never contains matching or business rules.
- See [the architecture document](architecture/architecture.md) for details.

## API and database
- Endpoints start with `/api` and use plural English names
  (e.g. `/api/degrees`, `/api/course-units`).
- JSON fields use camelCase.
- Database tables and columns use snake_case (e.g. `course_unit`).
- API errors use the same JSON format: `{ "status", "error", "message" }`.

## Testing
- Every business rule in a service or domain class has at least one unit test.
- Tests describe behaviour in their name (e.g. `registerRejectsDuplicateStudentNumber`).
- `./mvnw verify` must pass before a Pull Request is opened.

## Git workflow

### Issues
- Every piece of work starts with a GitHub Issue in the current Sprint milestone.
- The Issue has a short description of what is expected and at least one label
  (`backend`, `frontend`, `database`, `documentation`, `architecture`, `requirements`,
  `quality`, `setup`, `bug`, `enhancement`).
- Issues are assigned to one team member, who moves the card on the Project board
  (Todo → In Progress → In Review → Done).

### Branches
- Branch names: `<type>/<issue-number>-<short-description>`,
  e.g. `feature/11-end-to-end` or `docs/12-coding-conventions`.
- Types: `feature`, `fix`, `docs`, `test`, `chore`.
- Nobody commits directly to `main`.

### Commits
- Commit messages: `<type>: <description> (#<issue>)`,
  e.g. `docs: add coding conventions (#12)`.

### Pull Requests
- Every Pull Request links its Issue (`Closes #<number>`) and follows the Pull Request
  template.
- The CI checks (build, tests, lint) must pass.
- At least one team member who is not the author must review it.
- Nobody approves their own Pull Request.
- The reviewer checks that the code works, follows these conventions and has tests,
  and leaves comments when something should change. The author answers or fixes each
  comment before merging.
- After merge, the branch is deleted.

## Definition of Done
A task is done when:
- it has an Issue assigned to someone in the Sprint milestone
- the work was done on a correctly named branch
- the code compiles and runs, and the automated tests pass
- new business rules are covered by unit tests
- the code follows the conventions above
- the Pull Request links the Issue
- another team member reviewed and approved the Pull Request
- the documentation was updated if needed
- the branch was merged into `main` and deleted
- the Issue is closed and moved to Done on the board
