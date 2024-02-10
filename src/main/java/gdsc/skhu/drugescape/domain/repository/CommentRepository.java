package gdsc.skhu.drugescape.domain.repository;

import gdsc.skhu.drugescape.domain.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByBoardId(Long boardId); // 게시글 ID로 댓글 목록 조회
}