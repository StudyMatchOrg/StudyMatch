package pt.upt.studymatch.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pt.upt.studymatch.domain.Degree;

public interface DegreeRepository extends JpaRepository<Degree, Long> {

    List<Degree> findAllByOrderByCodeAsc();

    Optional<Degree> findByCode(String code);
}
