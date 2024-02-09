package gdsc.skhu.drugescape.service;

import gdsc.skhu.drugescape.domain.model.Board;
import gdsc.skhu.drugescape.domain.model.Heart;
import gdsc.skhu.drugescape.domain.model.Member;
import gdsc.skhu.drugescape.domain.repository.BoardRepository;
import gdsc.skhu.drugescape.domain.repository.HeartRepository;
import gdsc.skhu.drugescape.domain.repository.MemberRepository;
import gdsc.skhu.drugescape.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HeartService {
    private final HeartRepository heartRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    public HeartService(HeartRepository heartRepository, BoardRepository boardRepository, MemberRepository memberRepository) {
        this.heartRepository = heartRepository;
        this.boardRepository = boardRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public void addHeart(Long boardId, Long memberId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + boardId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 회원을 찾을 수 없습니다: " + memberId));
        if (!heartRepository.existsByBoardAndMember(board, member)) {
            Heart heart = new Heart(board, member);
            heartRepository.save(heart);
        }
    }

    @Transactional
    public void removeHeart(Long boardId, Long memberId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 게시글을 찾을 수 없습니다: " + boardId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 ID의 회원을 찾을 수 없습니다: " + memberId));
        heartRepository.findByBoardAndMember(board, member)
                .ifPresent(heartRepository::delete);
    }
}