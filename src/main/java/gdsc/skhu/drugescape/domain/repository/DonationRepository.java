package gdsc.skhu.drugescape.domain.repository;

import gdsc.skhu.drugescape.domain.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
    Optional<Donation> findByReportId(Long reportId);
}