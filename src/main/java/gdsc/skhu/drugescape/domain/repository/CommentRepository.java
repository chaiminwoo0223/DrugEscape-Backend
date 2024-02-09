package gdsc.skhu.drugescape.domain.repository;

import gdsc.skhu.drugescape.domain.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}