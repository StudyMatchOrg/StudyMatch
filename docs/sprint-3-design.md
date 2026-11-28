# Sprint 3 Design: Group Formation and Final Release

This document explains the main design decisions of Sprint 3: how teachers define where groups
are formed (projects), how StudyMatch forms the groups and explains them, how the mandatory
group rules are guaranteed, and the features the team added (login with roles, search,
competency and structure editing, CSV export). The student profile and the cold-start strategy
used as input are explained in [sprint-2-design.md](sprint-2-design.md). The architecture is in
[architecture.md](architecture/architecture.md), the concepts in the
[domain model](initial-domain-model.md) and the use cases in [use-cases.md](use-cases.md).

## 1. Projects: where groups are formed

Teachers form groups for a **project**, called a grouping context in the code
(`GroupingContext`, UC14). A project has:

| Information | Why |
|---|---|
| Name and description | Identifies the project (brief section 2.1). |
| Course unit and academic year (optional) | Used to load the students enrolled there with one click; a project can also be an assignment that is not tied to one course unit. |
| Eligible students | Every one of them is placed in a group (brief section 2.2). Any students can be chosen; a student cannot be chosen twice. |
| **Group size, chosen by the teacher** | Different projects need different sizes: a lab in pairs, a project in groups of 5. From 2 to 10; the form starts at 5. |
| Relevant competencies and their importance (0.1 to 1) | Different activities stress different competencies: a database project gives Databases a high importance. Groups are balanced only on these. |
| Owner and creation time | A project belongs to the teacher who created it. Other teachers cannot see it; an admin can. |

The moment the groups were generated (brief section 2.1) belongs to each generation, not to the
project, because a project can be generated many times (section 6).

## 2. How many groups, and of what size

The teacher chooses the group size; the sizes then vary as little as possible:

```
number of groups = ⌈students / size⌉     (never more than students / 2, so no one is alone)
group sizes      = as equal as possible, differing by at most 1, largest first
```

| Students | Size | Groups | Warning |
|---|---|---|---|
| 23 | 5 | 5 + 5 + 5 + 4 + 4 | no |
| 20 | 5 | 5 + 5 + 5 + 5 | no |
| 12 | 5 | 4 + 4 + 4 | no |
| 11 | 5 | 4 + 4 + 3 | yes |
| 7 | 5 | 4 + 3 | yes |
| 14 | 3 | 3 + 3 + 3 + 3 + 2 | no |
| 3 | 2 | 3 | yes |
| 0 or 1 | any | refused | – |

With the starting size of 5 this gives exactly the brief's mandatory rule: groups of 4 or 5
whenever that is mathematically possible (23 students give 5 + 5 + 5 + 4 + 4, never
5 + 5 + 5 + 5 + 3). When it is not possible, StudyMatch still forms the closest groups and
says why with a **warning** (e.g. "11 students cannot all be placed in groups of 4 or 5, so the
groups are 4 + 4 + 3."), which is shown and saved with the groups. Fewer than 2 students are
refused with a clear error. The rule lives in `GroupSizePlanner`.

## 3. The matching strategy

The brief asks every team to decide and justify four things. Our answers:

### 3.1 Which student characteristics are used

Only the **competency profile** from Sprint 2: for every competency relevant to the project,
the student's score (0 to 1) and its **confidence**. The profile already summarises the
academic history (grades, ECTS, retakes) and the teacher's initial assessment. Personal data
(name, age, degree, gender) is never used to form groups.

### 3.2 How they influence the matching

A score is only trusted as much as its confidence; the rest is filled in with the class
average (`MatchingProfileBuilder`):

```
effective score = confidence × score + (1 − confidence) × class average
```

This is how the **cold-start strategy meets group formation**. A first-year student with only a
teacher estimate (confidence 0.33) is placed mostly as an average student, and a competency
with no evidence counts exactly as the class average: nobody is treated as bad at something just
because there is no information. Students with no grades in any relevant competency are shown
as "no grades yet" in the groups, so the teacher can check them.

### 3.3 What makes a group appropriate

A good group can do every part of the project. For each relevant competency, the group's
**strength** is (`GroupScorer`):

```
strength = (best member + 0.5 × second-best member) / 1.5
```

- The **best member** counts most: one student who masters a competency can lead that part.
- The **second best** counts half, so the group does not depend on a single person.
- Further members add nothing: a third strong programmer does not make the group better at
  programming. This is what makes the algorithm prefer **complementary** students over several
  students with the same strength.

### 3.4 How the criteria are combined

- **Within a group:** the group score is the importance-weighted average of its strengths, using
  the importance the teacher gave each competency.
- **Between groups:** solutions are compared by their group scores **from the weakest up**
  (leximin): the solution whose weakest group is stronger wins; if equal, the second weakest
  decides, and so on. Maximising the average would accept one weak group if the others were
  very strong; a teacher prefers that every group can do the project.

### 3.5 Worked example

Four students, Programming and Mathematics equally important, groups of 2:

| Student | Programming | Mathematics |
|---|---|---|
| Ana | 0.9 | 0.4 |
| Rui | 0.8 | 0.5 |
| Eva | 0.4 | 0.9 |
| Tó | 0.5 | 0.8 |

| Grouping | Group scores | Weakest | Average |
|---|---|---|---|
| Similar students: {Ana, Rui}, {Eva, Tó} | 0.667, 0.667 | 0.667 | 0.667 |
| {Ana, Eva}, {Rui, Tó} | 0.733, 0.700 | 0.700 | 0.717 |
| **Complementary and balanced: {Ana, Tó}, {Rui, Eva}** | **0.717, 0.717** | **0.717** | 0.717 |

For example, {Ana, Tó}: Programming (0.9 + 0.5 × 0.5) / 1.5 = 0.767, Mathematics
(0.8 + 0.5 × 0.4) / 1.5 = 0.667, score 0.717. Putting similar students together gives the
worst result. The last two groupings have the same average; only the leximin comparison picks
the fairer one, where neither group is weaker.

### 3.6 The algorithm

`BalancedGroupingStrategy` works in two steps:

1. **Balanced start.** Students are ordered from strongest to weakest overall and dealt to the
   groups like cards, back and forth (1, 2, 3, 3, 2, 1, ...), so every group starts with a fair
   mix of strong and weaker students.
2. **Improvement by swaps.** Repeatedly make the single swap of two students between two groups
   that most improves the solution, until no swap helps. Swaps never change group sizes, so the
   mandatory rules stay true at every step.

The result is deterministic: the same students, profiles and seed give the same groups (the seed
only breaks ties between students with the same overall score). Each swap step tries every pair
of students in different groups, which takes milliseconds for a class of 23. A safety limit of
1 000 swaps keeps the time bounded for very large classes.

`RandomGroupingStrategy` forms random groups with the same sizes. It is what teachers often do
without a tool, so it is offered as an option and every generation reports the weakest group
that random groups would have had: the teacher sees what the balanced strategy gained.

Both strategies implement the `GroupingStrategy` interface, so another strategy (for example a
genetic algorithm) can be added without changing anything else.

## 4. The mandatory group rules (brief section 3.1)

`GroupFormation` checks the rules for **any** strategy, so a faulty strategy can never produce
invalid groups:

| Rule | How it is guaranteed | Tests |
|---|---|---|
| Every eligible student is considered | Every project student is given to the strategy, and the result is checked to contain all of them | `GroupFormationTest`, `GroupingStrategyTest` |
| A student belongs to only one group per generation | The result is checked for exactly one placement per student | `GroupingStrategyTest.everyStudentIsInExactlyOneGroupOfThePlannedSize` |
| The same student cannot appear twice | A student chosen twice is refused (400) when saving the project and when forming groups | `GroupingContextTest.aStudentCannotBeSelectedTwice`, `GroupFormationTest.aStudentSelectedTwiceIsRejected` |
| Groups of 4 or 5 whenever possible | `GroupSizePlanner` (section 2) | `GroupSizePlannerTest` |
| Impossible configurations handled explicitly | Warning saved with the groups; fewer than 2 students refused | `GroupSizePlannerTest.impossibleSplitsStillFormGroupsButCarryAWarning` |
| Generated groups are persisted | Every generation is saved as a `GroupSet` | `GroupGenerationApiIntegrationTest` |
| Previous groups can be consulted | History per project; each generation keeps a copy of its explanation | `GroupGenerationApiIntegrationTest.everyGenerationIsSavedAndCanBeConsultedLater` |

## 5. Explaining the groups (brief section 4)

The groups page shows:

- **For the whole generation:** the group sizes, the weakest group compared with random groups,
  the average group, the difference between the strongest and the weakest group, and any warning.
- **For each group:** its members with their strongest competency and a "no grades yet" badge for
  cold-start students; its strength in each relevant competency, with the member who carries it;
  its strengths (strongest competencies) and its gaps (relevant competencies below 0.5).

## 6. Saving and consulting groups

Every generation is saved as a `GroupSet` with its time, author, strategy, seed and group size,
its groups (`StudentGroup`), their members (`GroupMember`) and their strengths
(`GroupStrength`). The members' scores and the explanation are saved as a **copy**: when grades,
assessments or the project change later, earlier generations still show what the teacher saw.
The lecturer can change data and generate again; the new generation appears next to the old one.

A student who is in saved groups cannot be deleted (409), so that earlier groups stay complete.

## 7. Team extensions (brief section 5)

| Extension | What it adds |
|---|---|
| Login with roles | Admin and teacher accounts. Teachers only see their own projects. |
| Configurable group size | The teacher chooses it per project (section 2). |
| Several strategies and comparison | Balanced or random groups; every generation is compared with random groups. |
| Configurable competency importance and weights | Importance per project; competencies and course-unit weights can be edited (UC11). |
| Historical group information | Every generation is kept and can be consulted (section 6). |
| Student search and filters | Search ignoring accents, filters by degree, year, course unit, minimum competency score and "no grades yet" (UC16). |
| CSV export of groups | Opens directly in Excel with Portuguese settings (UC7). |
| Degrees and course units | Admins create them in the application instead of only through demo data. |

## 8. Security model

| Decision | Reason |
|---|---|
| Spring Security with signed tokens (JWT, HS256) | The frontend logs in once and sends `Authorization: Bearer <token>` with every request. The backend keeps no sessions. |
| No cookies, so no CSRF protection needed | CSRF attacks rely on the browser sending cookies automatically; a token in a header is never sent automatically. |
| Two roles; an admin can do everything a teacher can | Matches the use cases. Rules are written once, by URL, in `SecurityConfig`, so new endpoints are protected by default. |
| Another teacher's project answers 404, not 403 | It does not reveal that the project exists. |
| Passwords stored only as BCrypt hashes, at least 8 characters | A database leak does not reveal passwords. |
| Same answer and same time for a wrong username and a wrong password | Response time does not reveal which usernames exist. |
| Formula-like CSV values prefixed with `'` | A malicious student name cannot run a formula when the export is opened in Excel. |

Known limitations, also listed in the release notes: the token is kept in the browser's
`sessionStorage` (readable by scripts if the site ever had an XSS flaw); there is no limit on
login attempts; the application must be served over HTTPS in production; the demo accounts have
public demo passwords and must not be used outside development.

## 9. Changes to earlier tests (brief section 6)

- **Integration tests of Sprint 2 now log in.** The API now requires a login, so the existing
  API test classes run as an administrator (`@WithMockUser(roles = "ADMIN")`). The requirement
  changed, so the tests were updated, not deleted; their assertions are unchanged.
- **7 lost cold-start tests were restored.** When #52 was merged in Sprint 2, the file
  `EvidenceAggregatorTest.java` was overwritten by the cold-start tests and 7 tests of the
  evidence rule disappeared. They are back in `EvidenceAggregatorTest`, and the cold-start tests
  are in `ColdStartPolicyTest`, matching their class.
- **A defect found by manual testing got a regression test.** Saved groups could not be read
  after the transaction that loaded them had ended. The API tests did not notice, because each
  runs in one transaction; `GroupSetLoadingIntegrationTest` reproduces it without a transaction.

## 10. Tests of the new business logic

| Brief target | Tests |
|---|---|
| Matching calculations | `GroupScorerTest`, `MatchingProfileBuilderTest`, `GroupFormationTest.eachGroupExplainsItsStrengthsGapsAndWhoCarriesThem` |
| Group-size validation | `GroupSizePlannerTest` (23, 20, 12, 11, 7, 6, 3 students; sizes 1, 2, 10, 11; every combination up to 60 students) |
| Student eligibility | `GroupingContextApiIntegrationTest.candidatesAreTheStudentsEnrolledInTheCourseUnitThatYear`, `GroupingContextTest` |
| Student assignment | `GroupingStrategyTest`, `GroupFormationTest.twentyThreeStudentsFormFiveGroupsAndEveryoneIsPlacedOnce` |
| Duplicated students | `GroupingContextTest.aStudentCannotBeSelectedTwice`, `GroupFormationTest.aStudentSelectedTwiceIsRejected` |
| Cold start in group generation | `MatchingProfileBuilderTest`, `GroupFormationTest.lowEvidenceStudentsAreStillPlacedAndFlagged`, `GroupGenerationApiIntegrationTest.studentsWithoutGradesAreGroupedAndFlaggedAsLowEvidence` |
| Invalid configurations and boundaries | `GroupSizePlannerTest`, `GroupingContextApiIntegrationTest.invalidProjectsAreRejected`, `GroupGenerationApiIntegrationTest.aProjectWithFewerThanTwoStudentsCannotBeGrouped` |
| Team-specific rules | `AuthApiIntegrationTest`, `StudentSearchApiIntegrationTest`, `CompetencyEditingApiIntegrationTest`, `StructureAdminApiIntegrationTest`, `GroupExportApiIntegrationTest`, `StudentDeletionApiIntegrationTest` |

The whole suite runs with `./mvnw clean verify`, on an in-memory H2 database.

## 11. Alternatives considered

| Alternative | Why not |
|---|---|
| Groups of similar students | Easier to explain, but gives groups that cannot cover every part of the project and very unequal groups (section 3.5). |
| Maximise the average group score | Accepts one weak group if the others are strong; the leximin comparison protects the weakest group. |
| Count every member equally (average of the group) | Rewards groups of several students with the same strength and does not prefer complementary students. |
| Genetic algorithm | Possible later behind the same `GroupingStrategy` interface, but only worth it if the balanced strategy proves too weak; it is harder to explain and to test. |
| Exact optimisation (integer programming) | Needs an external solver and is slow for larger classes; the swap search is fast and good enough. |
| Fixed group size of 5 | The brief's rule, but teachers need other sizes; we keep 5 as the starting value and let the teacher choose. |

## 12. Demo data

When `studymatch.demo-data.enabled=true` (the default when running the application), the backend
also adds, if missing:

- **Accounts:** `admin` (administrator) and `teacher` and `teacher2` (teachers), with the demo
  passwords listed in the README.
- **22 more second-year students**, who with Ana Sousa are the **23 students enrolled in
  Algoritmos e Estruturas de Dados in 2026/2027**: strong programmers, strong mathematicians,
  all-rounders and students who struggle, and three transfer students with no grades (one with a
  teacher estimate of Programming and Problem Solving, one of Mathematics, one with nothing).
- **The project "AED 2026/27 – Project groups"**, owned by `teacher`, with group size 5 and
  Programming (1.0), Problem Solving (0.8), Mathematical Reasoning (0.6) and Software
  Engineering (0.4) as relevant competencies. Generating its groups gives 5 + 5 + 5 + 4 + 4.
