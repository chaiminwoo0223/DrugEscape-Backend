package gdsc.skhu.drugescape.domain.repository;

import gdsc.skhu.drugescape.domain.model.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
}