package gdsc.skhu.drugescape.domain.repository;

import gdsc.skhu.drugescape.domain.model.Management;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ManagementRepository extends JpaRepository<Management, Long> {
    Optional<Management> findByMemberId(Long memberId);

    Optional<Management> findByMemberIdAndLastManagedDate(Long memberId, LocalDate lastManagedDate);
}