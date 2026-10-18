package pt.upt.studymatch.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.upt.studymatch.domain.CompetencyAssessment;

public interface CompetencyAssessmentRepository extends JpaRepository<CompetencyAssessment, Long> {

    @Query("""
            select a from CompetencyAssessment a join fetch a.competency c
            where a.student.id = :studentId
            order by c.name
            """)
    List<CompetencyAssessment> findByStudent(@Param("studentId") Long studentId);

    boolean existsByStudentId(Long studentId);
}
