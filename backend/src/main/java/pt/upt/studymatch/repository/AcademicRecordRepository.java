package pt.upt.studymatch.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
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

    @Query("""
            select r from AcademicRecord r join fetch r.courseUnit cu
            where r.student.id in :studentIds
            order by r.academicYear, r.semester, cu.code
            """)
    List<AcademicRecord> findByStudentsInOrder(@Param("studentIds") Collection<Long> studentIds);

    @Query("""
            select distinct r.student.id from AcademicRecord r
            where r.courseUnit.id = :courseUnitId and r.status = pt.upt.studymatch.domain.RecordStatus.ENROLLED
            """)
    Set<Long> findStudentIdsEnrolledIn(@Param("courseUnitId") Long courseUnitId);

    boolean existsByStudentIdAndCourseUnitIdAndAcademicYearAndSemester(
            Long studentId, Long courseUnitId, String academicYear, int semester);

    boolean existsByStudentIdAndCourseUnitIdAndAcademicYearAndSemesterAndIdNot(
            Long studentId, Long courseUnitId, String academicYear, int semester, Long id);

    boolean existsByStudentId(Long studentId);

    void deleteByStudentId(Long studentId);
}
