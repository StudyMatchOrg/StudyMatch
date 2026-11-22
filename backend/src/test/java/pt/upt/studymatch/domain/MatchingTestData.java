package pt.upt.studymatch.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Small helpers to build students and criteria for the grouping tests. */
final class MatchingTestData {

    static final MatchingCriteria PROG_DATA_ALGO = new MatchingCriteria(List.of(
            new MatchingCriteria.Criterion("PROG", "Programming", 1.0),
            new MatchingCriteria.Criterion("DATA", "Databases", 1.0),
            new MatchingCriteria.Criterion("ALGO", "Algorithms", 1.0)));

    private MatchingTestData() {
    }

    static MatchingStudent student(long id, String name, double prog, double data, double algo) {
        return new MatchingStudent(id, String.valueOf(50000 + id), name,
                Map.of("PROG", prog, "DATA", data, "ALGO", algo), (prog + data + algo) / 3, false);
    }

    /** A class of students with varied, reproducible scores between 0.3 and 0.95. */
    static List<MatchingStudent> classOf(int size, long seed) {
        Random random = new Random(seed);
        List<MatchingStudent> students = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            students.add(student(i, "Student " + i, score(random), score(random), score(random)));
        }
        return students;
    }

    private static double score(Random random) {
        return 0.3 + random.nextDouble() * 0.65;
    }

    static List<Long> ids(List<MatchingStudent> group) {
        return group.stream().map(MatchingStudent::studentId).sorted().toList();
    }
}
