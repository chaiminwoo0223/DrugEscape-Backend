package gdsc.skhu.drugescape.domain.repository;

import gdsc.skhu.drugescape.domain.model.Board;
import gdsc.skhu.drugescape.domain.model.Heart;
import gdsc.skhu.drugescape.domain.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HeartRepository extends JpaRepository<Heart, Long> {
    // 특정 게시글과 멤버 기반으로 좋아요 존재 여부 확인
    boolean existsByBoardAndMember(Board board, Member member);

    // 특정 게시글과 멤버에 대한 좋아요 조회
    Optional<Heart> findByBoardAndMember(Board board, Member member);
}