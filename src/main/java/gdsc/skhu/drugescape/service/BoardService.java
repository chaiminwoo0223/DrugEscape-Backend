package gdsc.skhu.drugescape.service;

import gdsc.skhu.drugescape.domain.dto.BoardDTO;
import gdsc.skhu.drugescape.domain.dto.CommentDTO;
import gdsc.skhu.drugescape.domain.model.Board;
import gdsc.skhu.drugescape.domain.model.Comment;
import gdsc.skhu.drugescape.domain.model.Member;
import gdsc.skhu.drugescape.domain.repository.BoardRepository;
import gdsc.skhu.drugescape.domain.repository.CommentRepository;
import gdsc.skhu.drugescape.domain.repository.MemberRepository;
import gdsc.skhu.drugescape.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    public BoardService(BoardRepository boardRepository,
                        MemberRepository memberRepository,
                        CommentRepository commentRepository) {
        this.boardRepository = boardRepository;
        this.memberRepository = memberRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public Page<BoardDTO> getBoardList(Pageable pageable) {
        return boardRepository.findAll(pageable)
                .map(board -> {
                    List<Comment> comments = commentRepository.findByBoardId(board.getId());
                    List<CommentDTO> commentDTOs = comments.stream()
                            .map(CommentDTO::from) // from 메소드를 사용하여 CommentDTO 인스턴스 생성
                            .collect(Collectors.toList());
                    return new BoardDTO(
                            board.getId(),
                            board.getTitle(),
                            board.getContent(),
                            board.getMember().getName(), // 작성자 이름 추가
                            board.getHeartCnt(),
                            board.getCreatedAt().toString(),
                            board.getLastModifiedAt().toString(),
                            commentDTOs
                    );
                });
    }

    @Transactional
    public BoardDTO getBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + boardId));
        List<Comment> comments = commentRepository.findByBoardId(boardId); // 게시글 ID로 댓글 목록 조회
        List<CommentDTO> commentDTOs = comments.stream()
                .map(CommentDTO::from)
                .collect(Collectors.toList());
        return BoardDTO.from(board, commentDTOs); // 게시글 정보와 댓글 목록을 포함한 BoardDTO 반환
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
    public void updateBoard(Long boardId, BoardDTO boardDTO, Long memberId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + boardId));
        if (!board.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("게시글 수정 권한이 없습니다.");
        }
        board.updateDetails(boardDTO.getTitle(), boardDTO.getContent());
    }

    @Transactional
    public void deleteBoardIfOwner(Long boardId, Long memberId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + boardId));
        if (!board.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("게시글 삭제 권한이 없습니다.");
        }
        boardRepository.delete(board);
    }
}