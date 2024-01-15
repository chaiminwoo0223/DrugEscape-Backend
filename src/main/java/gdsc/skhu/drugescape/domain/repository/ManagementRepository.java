package gdsc.skhu.drugescape.domain.repository;

import gdsc.skhu.drugescape.domain.model.Management;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagementRepository  extends JpaRepository<Management, Long> {
}