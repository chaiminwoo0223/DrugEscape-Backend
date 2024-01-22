package gdsc.skhu.drugescape.domain.repository;

import gdsc.skhu.drugescape.domain.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportRepository  extends JpaRepository<Report, Long> {
    Optional<Report> findByMemberId(Long memberId);
}