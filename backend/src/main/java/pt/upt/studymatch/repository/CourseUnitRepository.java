package pt.upt.studymatch.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.upt.studymatch.domain.CourseUnit;

public interface CourseUnitRepository extends JpaRepository<CourseUnit, Long> {

    Optional<CourseUnit> findByCode(String code);

    @Query("select cu from CourseUnit cu order by cu.curricularYear, cu.semester, cu.code")
    List<CourseUnit> findAllInCurricularOrder();

    @Query("""
            select cu from CourseUnit cu join cu.degrees d
            where d.id = :degreeId
            order by cu.curricularYear, cu.semester, cu.code
            """)
    List<CourseUnit> findByDegreeInCurricularOrder(@Param("degreeId") Long degreeId);
}
