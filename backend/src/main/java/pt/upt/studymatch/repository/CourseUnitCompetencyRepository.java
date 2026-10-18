package pt.upt.studymatch.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.upt.studymatch.domain.CourseUnitCompetency;

public interface CourseUnitCompetencyRepository extends JpaRepository<CourseUnitCompetency, Long> {

    @Query("""
            select link from CourseUnitCompetency link
            join fetch link.courseUnit cu join fetch link.competency c
            order by cu.curricularYear, cu.semester, cu.code, link.weight desc, c.name
            """)
    List<CourseUnitCompetency> findAllInCurricularOrder();

    @Query("""
            select link from CourseUnitCompetency link
            join fetch link.courseUnit cu join fetch link.competency c
            where cu.id = :courseUnitId
            order by link.weight desc, c.name
            """)
    List<CourseUnitCompetency> findByCourseUnit(@Param("courseUnitId") Long courseUnitId);
}
