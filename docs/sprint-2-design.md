# Sprint 2 Design: Student Profile and Cold Start

This document explains the main design decisions of Sprint 2: how students and their
academic history are represented, how a competency profile is calculated from that history,
how StudyMatch handles students with little or no history (cold start), and the
team-defined feature. The architecture is described in
[architecture.md](architecture/architecture.md) and the concepts in the
[domain model](initial-domain-model.md).

## 1. Student information

A student has a **student number**, **name**, **email**, **degree** and **enrolment year**.

| Rule | Why |
|---|---|
| Student number and email are unique. | They identify the student in the university's systems and in imported files. |
| The degree is required and must exist. | Course units, and therefore competencies, depend on the degree. |
| The enrolment year cannot be after the current year. | A student cannot have enrolled in the future. |

Students can be created, viewed and updated, and are stored in MySQL between executions.

## 2. Academic history

The history is a list of **academic records**. A record is one attempt of a student at a
course unit, in a given academic year (e.g. `2025/2026`) and semester.

| Status | Grade | Meaning |
|---|---|---|
| `ENROLLED` | none | The student is currently taking the course unit. |
| `PASSED` | 10 to 20 | Finished and passed. |
| `FAILED` | 0 to 9 | Finished and failed. |

- A student can have **several records for the same course unit**: a failed attempt
  followed by a pass, or a grade improvement. This keeps the real academic path instead of
  overwriting it.
- There can only be **one record per course unit, year and semester**.
- The course unit must belong to the student's degree.

These rules live in the `AcademicRecord` entity, so they apply in the same way to the form
and to the CSV import.

## 3. Competencies

A **competency** is something a student can be good at, e.g. Programming, Databases or
Problem Solving. Competencies are data, not code, so new ones can be added without changing
the application.

Each course unit develops one or more competencies with a **weight** greater than 0 and at
most 1. For example, Algoritmos e Estruturas de Dados develops Algorithms (1.0), Problem
Solving (0.8) and Programming (0.5). A course unit that does not develop a competency
simply has no link to it, which is why 0 is not a valid weight. The full demo weight table
is visible in the application, on the **Competencies** page.

## 4. The student profile: one rule for all evidence

The profile gives each competency a **score** from 0 to 1 and a **confidence**. It is
calculated whenever it is requested, so it always reflects the latest data.

Everything known about a competency is treated as a piece of **evidence** with a value
and a weight:

| Evidence | Value | Weight |
|---|---|---|
| The best grade of a course unit linked to the competency | grade / 20 | competency weight × ECTS / 6 |
| A teacher's initial assessment (cold start) | 0 to 1 | 1 |

All evidence for a competency is combined by a single rule:

```
score      = Σ value × weight / Σ weight
confidence = W / (W + 2)           where W = Σ weight
```

| Decision | Reason |
|---|---|
| **Only the best grade of each course unit counts.** | In Portuguese universities a grade improvement or a passed retake replaces the earlier grade. A course unit that was only failed still counts, with its grade, because it is real evidence. |
| **ENROLLED records are not evidence.** | There is no result yet. |
| **The grade is normalised as grade / 20.** | Simple and explainable. A grade relative to the class (percentile) would be fairer between harsh and easy course units, but changing one student's grade would then change other students' profiles. That is confusing and hard to test. |
| **ECTS are part of the weight.** | A 7.5-ECTS course unit is more evidence than a 3-ECTS one. A standard 6-ECTS course unit fully about a competency has weight 1. |
| **No evidence means unknown, never 0.** | A missing grade says nothing about the student's ability. Unknown competencies are shown as "?" with the status *No evidence*. |
| **The confidence grows with the total weight.** | 1 standard course unit gives 0.33, 2 give 0.5, 3 give 0.6, 6 give 0.75. It never reaches 1, because more evidence always helps. |

Each competency also has a **status**: `ACADEMIC` (only grades), `INITIAL_ONLY` (only a
teacher assessment), `BLENDED` (both) or `UNKNOWN` (nothing). The profile view shows, for
every competency, each piece of evidence with its value, weight and share of the score.

### Worked example: Ana Sousa, Programming

Ana (demo student 52031) passed Introdução à Programação with 17 and Programação Orientada a
Objetos with 15. Both develop Programming with weight 1.0.

| Evidence | Value | Weight | Share |
|---|---|---|---|
| IP, 6 ECTS, grade 17 | 0.85 | 1.0 × 6/6 = 1.00 | 44% |
| POO, 7.5 ECTS, grade 15 | 0.75 | 1.0 × 7.5/6 = 1.25 | 56% |

score = (0.85 × 1.00 + 0.75 × 1.25) / 2.25 = **0.79**
confidence = 2.25 / (2.25 + 2) = **0.53**

Ana also failed Matemática Discreta with 8. That grade (0.40) is her only evidence for
Mathematical Reasoning, and also counts towards Problem Solving.

## 5. Cold-start strategy

**Question from the brief:** how can StudyMatch build a useful initial profile when reliable
academic history does not exist yet?

**Our answer:** the teacher gives an **initial assessment**, an estimate from 0 to 1, for
the competencies they could observe (e.g. in the first classes). That assessment is simply
one more piece of evidence, **worth as much as one standard course unit** (weight 1). The
same rule as above then does the rest:

| Academic evidence (total weight) | Share of the teacher assessment | Confidence |
|---|---|---|
| none | 100% | 0.33 |
| 1 course unit | 50% | 0.50 |
| 2 course units | 33% | 0.60 |
| 3 course units | 25% | 0.67 |
| 6 course units | 14% | 0.78 |

So when academic evidence starts to arrive, the estimate fades automatically, and the
confidence shows how much the score can be trusted.

**Example: Bruno Costa (demo student 53007).** Bruno is a first-year student with no grades.
The teacher assessed Programming at 0.6.

- **Before any grade:** Programming = 0.60, confidence 0.33, status *Teacher estimate*.
- **He passes Introdução à Programação with 16** (value 0.8, weight 1): Programming =
  (0.6 × 1 + 0.8 × 1) / 2 = **0.70**, confidence 0.50, status *Grades + estimate*.

**Why this strategy:**

- **One rule instead of two.** An earlier idea had a separate blending formula,
  `final = α × academic + (1 − α) × initial` with `α = n / (n + 2)`. In that version the
  teacher assessment counted as two course units in the score, but as half a course unit in
  the confidence. Treating it as evidence makes both consistent by construction.
- **It is a known technique:** in statistics this is called *shrinkage towards a prior*.
  The prior (the teacher's estimate) counts as a fixed amount of pseudo-evidence.
- **It is explainable:** every score can be broken down into the exact share of each grade
  and of the teacher's estimate.
- **It is extensible:** another source, such as a self-assessment or secondary-school
  grades, is just a new evidence type with its own weight (e.g. 0.5 for a
  self-assessment, which is less reliable than a teacher's observation).

**Alternatives considered:**

| Alternative | Why we did not choose it |
|---|---|
| Random initial profile | Not explainable or testable, and it would mislead group formation. |
| Score 0 for missing competencies | Treats "we don't know" as "bad at it". |
| Class average as the initial value | Hides the fact that nothing is known about the student. It may still be useful for group formation (see section 9). |

## 6. Missing and invalid information

| Situation | Behaviour |
|---|---|
| No evidence for a competency | Score unknown (`null`), status `UNKNOWN`, confidence 0. |
| Only ENROLLED records | Not evidence yet, so they don't change the profile. |
| Grade outside 0–20, or not matching the status | Rejected with a clear message, e.g. "A PASSED record needs a grade of at least 10, but was 8". |
| The same attempt recorded twice | Rejected as a conflict (409). |
| Assessment value outside 0–1 | Rejected. |
| Invalid lines in a CSV import | Reported with their line number and reason; the valid lines are still imported. |

## 7. Team-defined feature: CSV import of academic history

**What it does:** the teacher uploads a CSV file with one academic record per line
(`studentNumber,courseUnitCode,academicYear,semester,status,grade`). StudyMatch saves the
valid lines and lists each rejected line with the reason.

**Why it adds value:** in a real class there are dozens of students with several years of
grades, and typing them in one by one is slow and error-prone. Importing makes StudyMatch
usable with real data, and the per-line report improves data quality.

**Design decisions:**

- Each line goes through the same `AcademicRecordService.record(...)` method as the form,
  so the rules can never differ between the two.
- Each line is saved in its own transaction, so one bad line never cancels the good ones.
- Commas and semicolons are both accepted (Excel in Portuguese exports with `;`), columns
  can be in any order, and blank lines are ignored.
- A sample file with deliberately invalid lines is available on the Import page.

## 8. Tests

The backend has **104 automated tests** (JUnit 5, Mockito, Spring Boot Test with an
in-memory H2 database), run with `./mvnw clean verify`. The test types required by the
brief (section 5.1):

| Required | Examples |
|---|---|
| Normal scenario | `StudentServiceTest.registerSavesNormalisedStudent`, `ProfileCalculatorTest.scoreIsTheWeightedAverageOfTheGrades` |
| Invalid or exceptional scenario | `AcademicRecordTest.gradeAboveTwentyIsRejected`, `AcademicRecordTest.passedRecordWithFailingGradeIsRejected`, `ImportApiIntegrationTest.validLinesAreImportedAndInvalidLinesAreReported` |
| Boundary condition | `AcademicRecordTest.gradesAtTheBoundariesAreAccepted` (10, 20, 9, 0), `AcademicRecordTest.gradesJustOutsideTheBoundariesAreRejected`, `CourseUnitCompetencyTest` (weights 0, 1, 1.01) |
| Rule of the student profile | `ProfileCalculatorTest`, `AcademicEvidenceTest.passedRetakeReplacesTheFailedAttempt`, `AcademicEvidenceTest.weightIsCompetencyWeightTimesEctsOverSix` |
| Rule of the cold-start strategy | `EvidenceAggregatorTest.onlyATeacherAssessmentGivesItsValueWithLowConfidence`, `EvidenceAggregatorTest.teacherAssessmentCountsLessAsGradesArrive`, `EvidenceAggregatorTest.noEvidenceMeansUnknownNotZero` |

## 9. Looking ahead: group formation

Group formation is out of scope for Sprint 2. The profile was designed as its input: every
competency has a score and a confidence. The team's proposal for a later sprint:

- **The problem:** split the students of a course unit into groups (a constrained
  partitioning problem).
  - **Hard constraints** must always hold: every eligible student is in exactly one group,
    and group sizes differ by at most one.
  - **Soft objectives** are optimised: cover the competencies the teacher marks as important
    for the activity, combine complementary students, and keep the groups balanced.
- **Uncertain scores:** a low-confidence score is pulled towards the class average before
  matching, so a first-year teacher estimate does not count as much as solid grades.
- **Group quality:** use the best member and, partly, the second-best member for each
  competency, so that one strong student does not hide three weak ones.
- **Fairness between groups:** make the weakest group as strong as possible, rather than
  maximising the average.
- **Algorithm:** a version 1 builds valid groups greedily, then improves them by swapping
  students between groups while the result improves, with a fixed random seed so results
  are reproducible.
- **Evaluation:** compare the result against random groups, which is what teachers do today.
  A genetic algorithm is only worth trying if the simple approach proves too weak.
- **Explanation:** each group comes with its strengths and gaps, so the teacher can
  understand and adjust it.
- **Architecture:** the algorithm sits behind a `GroupingStrategy` interface, so it can be
  replaced without changing the rest of the application.

## 10. Demo data

When `studymatch.demo-data.enabled=true` (the default for running the application), the
backend adds the following if it is missing:

- 2 degrees and 9 course units
- 8 competencies with their weights
- 4 students:
  - **Diogo Pereira (50112):** 3rd year, with a failed and then passed Matemática Discreta.
  - **Ana Sousa (52031):** 2nd year, Scenario A.
  - **Bruno Costa (53007):** 1st year with a teacher assessment, Scenario B.
  - **Carla Mendes (53015):** 1st year with no information, used for the CSV import in
    Scenario C.

Data added or changed in the application is kept between restarts.
