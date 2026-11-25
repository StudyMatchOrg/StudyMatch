package pt.upt.studymatch.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pt.upt.studymatch.domain.GroupSet;

public interface GroupSetRepository extends JpaRepository<GroupSet, Long> {

    List<GroupSet> findByContextIdOrderByGeneratedAtDescIdDesc(Long contextId);

    long countByContextId(Long contextId);
}
