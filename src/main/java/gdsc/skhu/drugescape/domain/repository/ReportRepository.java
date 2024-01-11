package gdsc.skhu.drugescape.domain.repository;

import gdsc.skhu.drugescape.domain.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository  extends JpaRepository<Report, Long> {
}