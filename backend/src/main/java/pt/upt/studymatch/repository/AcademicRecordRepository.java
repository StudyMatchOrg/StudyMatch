package pt.upt.studymatch.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.upt.studymatch.domain.AcademicRecord;

public interface AcademicRecordRepository extends JpaRepository<AcademicRecord, Long> {

    @Query("""
            select r from AcademicRecord r join fetch r.courseUnit cu
            where r.student.id = :studentId
            order by r.academicYear, r.semester, cu.code
            """)
    List<AcademicRecord> findByStudentInOrder(@Param("studentId") Long studentId);

    boolean existsByStudentIdAndCourseUnitIdAndAcademicYearAndSemester(
            Long studentId, Long courseUnitId, String academicYear, int semester);

    boolean existsByStudentIdAndCourseUnitIdAndAcademicYearAndSemesterAndIdNot(
            Long studentId, Long courseUnitId, String academicYear, int semester, Long id);

    boolean existsByStudentId(Long studentId);
}
