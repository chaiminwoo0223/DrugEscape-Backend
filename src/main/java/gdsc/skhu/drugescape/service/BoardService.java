package gdsc.skhu.drugescape.service;

import gdsc.skhu.drugescape.domain.dto.BoardDTO;
import gdsc.skhu.drugescape.domain.model.Board;
import gdsc.skhu.drugescape.domain.model.Member;
import gdsc.skhu.drugescape.domain.repository.BoardRepository;
import gdsc.skhu.drugescape.domain.repository.MemberRepository;
import gdsc.skhu.drugescape.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    public BoardService(BoardRepository boardRepository, MemberRepository memberRepository) {
        this.boardRepository = boardRepository;
        this.memberRepository = memberRepository;
    }

    public Page<Board> getBoardList(PageRequest pageRequest) {
        return boardRepository.findAll(pageRequest);
    }

    public BoardDTO getBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .map(BoardDTO::of)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + boardId));
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
    public Long updateBoard(Long boardId, BoardDTO boardDTO) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + boardId));

        board.updateDetails(boardDTO.getTitle(), boardDTO.getContent());
        return boardRepository.save(board).getId();
    }

    @Transactional
    public void deleteBoard(Long boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + boardId);
        }
        boardRepository.deleteById(boardId);
    }
}