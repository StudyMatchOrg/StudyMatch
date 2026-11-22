package pt.upt.studymatch.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.upt.studymatch.domain.GroupingContext;
import pt.upt.studymatch.domain.Student;

public interface GroupingContextRepository extends JpaRepository<GroupingContext, Long> {

    List<GroupingContext> findAllByOrderByCreatedAtDesc();

    List<GroupingContext> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Optional<GroupingContext> findFirstByName(String name);

    /** Students currently enrolled in a course unit in an academic year: the usual candidates for a project. */
    @Query("""
            select s from Student s
            where exists (
                select r from AcademicRecord r
                where r.student = s and r.courseUnit.id = :courseUnitId and r.academicYear = :academicYear
                  and r.status = pt.upt.studymatch.domain.RecordStatus.ENROLLED)
            order by s.name
            """)
    List<Student> findEnrolledStudents(@Param("courseUnitId") Long courseUnitId,
            @Param("academicYear") String academicYear);
}
