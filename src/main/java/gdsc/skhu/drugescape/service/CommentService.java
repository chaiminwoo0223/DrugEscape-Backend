package gdsc.skhu.drugescape.service;

import gdsc.skhu.drugescape.domain.model.Board;
import gdsc.skhu.drugescape.domain.model.Comment;
import gdsc.skhu.drugescape.domain.model.Member;
import gdsc.skhu.drugescape.domain.repository.BoardRepository;
import gdsc.skhu.drugescape.domain.repository.CommentRepository;
import gdsc.skhu.drugescape.domain.repository.MemberRepository;
import gdsc.skhu.drugescape.exception.ResourceNotFoundException;
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
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("해당 ID의 댓글을 찾을 수 없습니다: " + commentId);
        }
        commentRepository.deleteById(commentId);
    }

    @Transactional
    public boolean isCommentOwner(Long commentId, Long memberId) {
        return commentRepository.findById(commentId)
                .map(Comment::getMember)
                .map(Member::getId)
                .filter(id -> id.equals(memberId))
                .isPresent();
    }

}