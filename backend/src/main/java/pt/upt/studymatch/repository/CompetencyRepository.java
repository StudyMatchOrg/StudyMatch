package pt.upt.studymatch.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pt.upt.studymatch.domain.Competency;

public interface CompetencyRepository extends JpaRepository<Competency, Long> {

    List<Competency> findAllByOrderByNameAsc();

    Optional<Competency> findByCode(String code);
}
