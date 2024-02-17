package gdsc.skhu.drugescape.domain.repository;

import gdsc.skhu.drugescape.domain.model.Board;
import gdsc.skhu.drugescape.domain.model.Heart;
import gdsc.skhu.drugescape.domain.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HeartRepository extends JpaRepository<Heart, Long> {
    boolean existsByBoardAndMember(Board board, Member member);

    Optional<Heart> findByBoardAndMember(Board board, Member member);

    List<Heart> findByBoard(Board board);
}