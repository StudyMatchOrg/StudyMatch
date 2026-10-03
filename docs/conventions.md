# Coding Conventions and Definition of Done

##  Naming
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
| Tipo de Conta | `AccountType` |
| Curso | `Degree` |
| Cadeira | `CourseUnit` |
| Grupo | `Group` |
| Conjunto de Grupos | `GroupSet` |


## Project structure
- Backend packages: 
    `controller : recives the HTTP request`, 
    `service : business logic and rules`, 
    `repository : acess to database`, 
    `domain : entities ( Degree , CourseUnit ) `, 
    `dto : objects sent to frontend`
- Frontend folders: 
    `components : reusable parts `,
    `pages : one screen per file `,
    `services : API calls`
- Controllers contain no business logic. Business rules live in `service`.
- Frontend code never contains matching or business rules.



## API and database
- Endpoints start with `/api` and use plural English names 
  (e.g. `/api/degrees`, `/api/course-units`).
- JSON fields use camelCase.
- Database tables and columns use snake_case (e.g. `course_unit`).




## Git
- Branch names: `<type>/<issue-number>-<short-description>`, e.g. `feature/11-end-to-end` or `docs/12-coding-conventions`.
- Commit messages: `<type>: <description> (#<issue>)`, e.g. `docs: add coding conventio(#12)`.
- Pull Request rules:
  - nobody commits directly to `main`;
  - every Pull Request links its Issue (`Closes #<number>`);
  - at least one team member who is not the author must review it;
  - nobody approves their own Pull Request.
  - after merge the branch is deleted.


## Definition of Done
A task is done when:
- it has an Issue assigned to someone in the Sprint 1 milestone
- the work was done on a correctly named branch
- the code compiles and runs
- the code follows the conventions above
- the Pull Request links the Issue
- another team member reviewed and approved the Pull Request
- the documentation was updated if needed
- the branch was merged into `main` and deleted
- the Issue is closed and moved to Done on the board