package gdsc.skhu.drugescape.service;

import gdsc.skhu.drugescape.domain.dto.BoardDTO;
import gdsc.skhu.drugescape.domain.dto.CommentDTO;
import gdsc.skhu.drugescape.domain.model.*;
import gdsc.skhu.drugescape.domain.repository.BoardRepository;
import gdsc.skhu.drugescape.domain.repository.CommentRepository;
import gdsc.skhu.drugescape.domain.repository.HeartRepository;
import gdsc.skhu.drugescape.domain.repository.MemberRepository;
import gdsc.skhu.drugescape.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final HeartRepository heartRepository;

    @Transactional
    public Page<BoardDTO> getBoardList(Pageable pageable) {
        return boardRepository.findAll(pageable)
                .map(this::createBoardDTO);
    }

    @Transactional
    public BoardDTO getBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + boardId));
        return createBoardDTO(board);
    }

    @Transactional
    public Long createBoard(BoardDTO boardDTO, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 회원을 찾을 수 없습니다: " + memberId));
        Board board = Board.builder()
                .title(boardDTO.getTitle())
                .content(boardDTO.getContent())
                .member(member)
                .build();
        return boardRepository.save(board).getId();
    }

    @Transactional
    public void deleteBoardIfOwner(Long boardId, Long memberId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 게시글을 찾을 수 없습니다: " + boardId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 회원을 찾을 수 없습니다: " + memberId));
        if (isAuthorizedToDelete(member, board)) { // 관리자 또는 게시글 소유자일 경우 삭제 실행
            commentRepository.deleteByBoardId(boardId); // 게시글에 달린 모든 댓글 삭제
            List<Heart> hearts = heartRepository.findByBoard(board); // 게시글에 대한 모든 좋아요 삭제
            heartRepository.deleteAll(hearts); // 게시글에 달린 모든 좋아요 조회
            boardRepository.delete(board); // 게시글 삭제
        } else {
            throw new AccessDeniedException("게시글 삭제 권한이 없습니다.");
        }
    }

    private boolean isAuthorizedToDelete(Member member, Board board) {
        return member.getRole().equals(Role.ROLE_ADMIN) || board.getMember().getId().equals(member.getId());
    }

    private BoardDTO createBoardDTO(Board board) { // 공통 BoardDTO 생성 로직
        List<Comment> comments = commentRepository.findByBoardId(board.getId());
        List<CommentDTO> commentDTOs = comments.stream()
                .map(CommentDTO::from)
                .collect(Collectors.toList());
        return BoardDTO.from(board, commentDTOs);
    }
}