package pt.upt.studymatch.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.upt.studymatch.domain.GroupMember;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    boolean existsByStudentId(Long studentId);
}
