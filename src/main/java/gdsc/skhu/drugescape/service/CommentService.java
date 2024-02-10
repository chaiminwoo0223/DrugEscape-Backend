package gdsc.skhu.drugescape.service;

import gdsc.skhu.drugescape.domain.model.Board;
import gdsc.skhu.drugescape.domain.model.Comment;
import gdsc.skhu.drugescape.domain.model.Member;
import gdsc.skhu.drugescape.domain.model.Role;
import gdsc.skhu.drugescape.domain.repository.BoardRepository;
import gdsc.skhu.drugescape.domain.repository.CommentRepository;
import gdsc.skhu.drugescape.domain.repository.MemberRepository;
import gdsc.skhu.drugescape.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    public CommentService(CommentRepository commentRepository, BoardRepository boardRepository, MemberRepository memberRepository) {
        this.commentRepository = commentRepository;
        this.boardRepository = boardRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public void addComment(Long boardId, Long memberId, String content) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + boardId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 회원을 찾을 수 없습니다: " + memberId));
        Comment comment = new Comment(board, member, content);
        commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long boardId, Long commentId, Long memberId) {
        Comment comment = commentRepository.findByIdAndBoardId(commentId, boardId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 댓글을 찾을 수 없습니다: " + commentId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 회원을 찾을 수 없습니다: " + memberId));
        if (isAuthorizedToDelete(member, comment)) { // 관리자 또는 댓글 소유자일 경우 삭제 실행
            commentRepository.delete(comment);
        } else {
            throw new AccessDeniedException("댓글 삭제 권한이 없습니다.");
        }
    }

    private boolean isAuthorizedToDelete(Member member, Comment comment) {
        return member.getRole().equals(Role.ROLE_ADMIN) || comment.getMember().getId().equals(member.getId());
    }
}