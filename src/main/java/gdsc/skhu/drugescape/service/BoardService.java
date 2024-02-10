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

    public Page<Board> getBoardList(Pageable pageable) {
        return boardRepository.findAll(pageable);
    }

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
    public Long updateBoard(Long boardId, BoardDTO boardDTO, Long memberId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + boardId));
        if (!board.getMember().getId().equals(memberId)) {
            return -1L; // 권한이 없을 경우 음수 값을 반환하여 권한 없음을 표시
        }
        board.updateDetails(boardDTO.getTitle(), boardDTO.getContent());
        return boardRepository.save(board).getId();
    }

    @Transactional
    public boolean deleteBoardIfOwner(Long boardId, Long memberId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + boardId));
        if (!board.getMember().getId().equals(memberId)) {
            return false; // 권한이 없으면 false 반환
        }
        boardRepository.delete(board);
        return true; // 성공적으로 삭제하면 true 반환
    }
}