# StudyMatch Technology Stack

| Layer | Technology | Version |
|---|---|---|
| Frontend | React with Vite (JavaScript) | React 19, Vite 8 |
| Backend | Java with Spring Boot | Java 21, Spring Boot 4.1 |
| API | REST with JSON | — |
| Persistence | MySQL with Spring Data JPA and Hibernate | MySQL 8.4 |
| Build and dependencies | Maven (through the Maven Wrapper) and npm | Maven 3.10 |
| Testing and quality | JUnit 5, Mockito, H2, JaCoCo, oxlint, GitHub Actions | — |
| Version control and project management | Git and GitHub | — |

## Frontend

StudyMatch uses React with Vite for the frontend application.

React provides a component-based approach that supports modularity, maintainability
and reuse of user-interface components. It has a large ecosystem and is well
documented. Vite provides a fast development server with instant reload and a simple
production build, and needs almost no configuration.

## Backend

The backend is implemented in Java with Spring Boot.

Spring Boot provides a structured environment for developing REST APIs and integrates
with persistence, validation and testing tools out of the box. Its dependency injection
makes it easy to replace dependencies with test doubles, which supports unit testing.
Java and Spring Boot are the technologies the team knows best from previous course
units, which reduces risk.

## API

The frontend and backend communicate through a REST API, with JSON as the data
exchange format. REST is simple, widely understood and easy to test with tools such as
the browser, `curl` or automated integration tests.

## Persistence

MySQL is used as the relational database management system. StudyMatch data (students,
course units, grades and competencies) is naturally relational, and the team already
has experience with MySQL.

Spring Data JPA, with Hibernate as the implementation, is used for persistence in the
backend. It integrates naturally with Spring Boot and removes most of the repetitive
data-access code, so the team can focus on business logic.

## Build and dependency management

Maven is used for backend build and dependency management, and npm for the frontend.
Maven is the build tool the team has used in previous projects and is fully supported
by Spring Boot and by IDEs.

The repository includes the Maven Wrapper (`mvnw`), so every team member and the CI
server build with the same Maven version without installing it.

## Testing and quality

| Tool | Purpose |
|---|---|
| JUnit 5 | Automated unit and integration tests for the backend. |
| Mockito | Replaces dependencies such as repositories with test doubles in unit tests. |
| H2 (tests only) | In-memory database, so the tests run on any machine without MySQL. |
| JaCoCo | Measures test coverage on every backend build. |
| oxlint | Lints the frontend code. Included by default in the Vite React template and very fast. |
| GitHub Actions | Builds and tests every Pull Request automatically. |

All of these integrate with the chosen build tools and require no paid services.

## Version control and project management

Git is used for version control, and GitHub for repository hosting and project
management (Issues, Project board, labels and milestones).

Development follows the agreed workflow:

Issue → Branch → Implementation → Pull Request → Review → Merge

The details are in [conventions.md](conventions.md).

## Summary of the choice

| Criterion | How the stack meets it |
|---|---|
| Suitability | A web frontend, a REST backend and a relational database fit an application that manages structured academic data. |
| Maintainability | Clear separation between frontend, backend and database, and a layered backend. |
| Testability | JUnit, Mockito and Spring Boot's test support, plus an in-memory database for fast tests. |
| Integration | Spring Boot integrates JPA, validation and testing. Vite forwards API calls to the backend during development. |
| Team knowledge | The team has used Java, Spring Boot, Maven and MySQL in previous course units. |
| Tooling | Free, widely used tools with good IDE support and documentation. |
