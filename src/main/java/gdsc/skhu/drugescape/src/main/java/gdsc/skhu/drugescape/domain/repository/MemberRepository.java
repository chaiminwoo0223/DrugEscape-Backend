package gdsc.skhu.drugescape.domain.repository;

import gdsc.skhu.drugescape.domain.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
}