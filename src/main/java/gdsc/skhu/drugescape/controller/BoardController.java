package gdsc.skhu.drugescape.controller;

import gdsc.skhu.drugescape.domain.dto.BoardDTO;
import gdsc.skhu.drugescape.domain.dto.ResponseErrorDTO;
import gdsc.skhu.drugescape.domain.model.Board;
import gdsc.skhu.drugescape.service.BoardService;
import gdsc.skhu.drugescape.service.CommentService;
import gdsc.skhu.drugescape.service.HeartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.PageRequest;

import java.security.Principal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

// "admin을 어떻게 처리할 것인가?"는 아직 안했다. --> 반드시 추가! --> 가장 마지막 수정!
// 신고 기능도 아직 구현하지 않았다!
// 검색 기능도 아직 구현하지 않았다!
// 게시글 삭제할 때, 자신이 게시한 글만 삭제할 수 있도록 반드시 수정해야 한다.
// 이미지 처리 --> 웹과 협의!
@Slf4j
@RestController
@RequestMapping("/drugescape")
@RequiredArgsConstructor
@Tag(name = "Board API", description = "게시판 관련 API")
public class BoardController {
    private final BoardService boardService;
    private final CommentService commentService;
    private final HeartService heartService;

    // 페이지와 사이즈를 처리하는 것도 매우 중요
    @GetMapping("/share")
    public ResponseEntity<Page<Board>> getBoardList(@RequestParam int page, @RequestParam int size) {
        try {
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<Board> boardPage = boardService.getBoardList(pageRequest);
            return new ResponseEntity<>(boardPage, HttpStatus.OK);
        } catch (Exception e) {
            log.error("게시글 리스트 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "게시글 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class)))
    })
    @GetMapping("/share/{boardId}")
    public ResponseEntity<BoardDTO> getBoard(@PathVariable Long boardId) {
        try {
            BoardDTO boardDTO = boardService.getBoard(boardId);
            return new ResponseEntity<>(boardDTO, HttpStatus.OK);
        } catch (Exception e) {
            log.error("게시글 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "게시글 생성", description = "새로운 게시글을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/share")
    public ResponseEntity<Long> createBoard(Principal principal, @RequestBody BoardDTO boardDTO) {
        try {
            Long memberId = Long.parseLong(principal.getName());
            Long boardId = boardService.createBoard(boardDTO, memberId);
            return new ResponseEntity<>(boardId, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("게시글 생성 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "게시글 수정", description = "특정 게시글을 수정합니다.")
    @PutMapping("/share/{boardId}")
    public ResponseEntity<Long> updateBoard(@PathVariable Long boardId, @RequestBody BoardDTO boardDTO) {
        try {
            Long updatedBoardId = boardService.updateBoard(boardId, boardDTO);
            return new ResponseEntity<>(updatedBoardId, HttpStatus.OK);
        } catch (Exception e) {
            log.error("게시글 수정 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "게시글 삭제", description = "특정 게시글을 삭제합니다.")
    @DeleteMapping("/share/{boardId}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long boardId) {
        try {
            boardService.deleteBoard(boardId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            log.error("게시글 삭제 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "댓글 추가", description = "특정 게시글에 댓글을 추가합니다.")
    @PostMapping("/share/{boardId}/comments")
    public ResponseEntity<Void> addComment(Principal principal, @PathVariable Long boardId, @RequestParam String content) {
        try {
            Long memberId = Long.parseLong(principal.getName());
            commentService.addComment(boardId, memberId, content);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("댓글 추가 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "댓글 삭제", description = "특정 댓글을 삭제합니다.")
    @DeleteMapping("/share/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        try {
            commentService.deleteComment(commentId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            log.error("댓글 삭제 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "좋아요 추가", description = "특정 게시글에 좋아요를 추가합니다.")
    @PostMapping("/share/{boardId}/hearts")
    public ResponseEntity<Void> addHeart(Principal principal, @PathVariable Long boardId) {
        try {
            Long memberId = Long.parseLong(principal.getName());
            heartService.addHeart(boardId, memberId);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("좋아요 추가 중 에러 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "좋아요 삭제", description = "특정 게시글에 추가된 좋아요를 삭제합니다.")
    @DeleteMapping("/share/{boardId}/hearts")
    public ResponseEntity<Void> removeHeart(Principal principal, @PathVariable Long boardId) {
        try {
            Long memberId = Long.parseLong(principal.getName());
            heartService.removeHeart(boardId, memberId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            log.error("좋아요 삭제 중 에러 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}