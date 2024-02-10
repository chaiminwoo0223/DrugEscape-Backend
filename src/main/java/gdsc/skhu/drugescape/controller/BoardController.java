package gdsc.skhu.drugescape.controller;

import gdsc.skhu.drugescape.domain.dto.BoardDTO;
import gdsc.skhu.drugescape.domain.dto.CommentDTO;
import gdsc.skhu.drugescape.exception.ResourceNotFoundException;
import gdsc.skhu.drugescape.service.BoardService;
import gdsc.skhu.drugescape.service.CommentService;
import gdsc.skhu.drugescape.service.HeartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

// "admin을 어떻게 처리할 것인가?"는 아직 안했다. --> 반드시 추가! --> 가장 마지막 수정! - 2
// 검색 기능도 아직 구현하지 않았다! - 1
// 댓글 수도 추가하자! - 2
// 이미지 처리 --> 웹과 협의! --> 상담 요청! - 5
@Slf4j
@RestController
@RequestMapping("/drugescape")
@RequiredArgsConstructor
@Tag(name = "Board API", description = "게시판 관련 API")
public class BoardController {
    private final BoardService boardService;
    private final CommentService commentService;
    private final HeartService heartService;

    @Operation(summary = "게시글 리스트 조회", description = "게시판의 모든 게시글을 페이지별로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/share")
    public ResponseEntity<Page<BoardDTO>> getBoardList(Pageable pageable) {
        try {
            Page<BoardDTO> boardPage = boardService.getBoardList(pageable);
            return ResponseEntity.ok(boardPage);
        } catch (Exception e) {
            log.error("게시글 리스트 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "게시글 조회", description = "게시글의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @GetMapping("/share/{boardId}")
    public ResponseEntity<BoardDTO> getBoard(@PathVariable Long boardId) {
        try {
            BoardDTO boardDTO = boardService.getBoard(boardId);
            return ResponseEntity.ok(boardDTO);
        } catch (ResourceNotFoundException e) {
            log.error("게시글 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("게시글 조회 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @Operation(summary = "게시글 생성", description = "새 게시글을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/share/post")
    public ResponseEntity<?> createBoard(Principal principal, @RequestBody BoardDTO boardDTO) {
        try {
            Long memberId = Long.parseLong(principal.getName());
            Long boardId = boardService.createBoard(boardDTO, memberId);
            return new ResponseEntity<>(boardId, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("게시글 생성 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "게시글 수정", description = "게시글을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @PutMapping("/share/{boardId}")
    public ResponseEntity<?> updateBoard(Principal principal, @PathVariable Long boardId, @RequestBody BoardDTO boardDTO) {
        Long memberId = Long.parseLong(principal.getName());
        try {
            boardService.updateBoard(boardId, boardDTO, memberId);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            log.error("게시글 수정 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            log.error("게시글 수정 권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("게시글 수정 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @DeleteMapping("/share/{boardId}")
    public ResponseEntity<?> deleteBoard(Principal principal, @PathVariable Long boardId) {
        Long memberId = Long.parseLong(principal.getName());
        try {
            boardService.deleteBoardIfOwner(boardId, memberId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            log.error("게시글 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            log.error("게시글 삭제 권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("게시글 삭제 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "좋아요 추가", description = "게시글에 좋아요를 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "좋아요 추가 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @PostMapping("/share/{boardId}/hearts")
    public ResponseEntity<?> addHeart(Principal principal, @PathVariable Long boardId) {
        Long memberId = Long.parseLong(principal.getName());
        try {
            heartService.addHeart(boardId, memberId);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            log.error("좋아요 추가 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("좋아요 추가 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "좋아요 삭제", description = "게시글의 좋아요를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "좋아요 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @DeleteMapping("/share/{boardId}/hearts")
    public ResponseEntity<?> removeHeart(Principal principal, @PathVariable Long boardId) {
        Long memberId = Long.parseLong(principal.getName());
        try {
            heartService.removeHeart(boardId, memberId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ResourceNotFoundException e) {
            log.error("좋아요 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("좋아요 삭제 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "댓글 추가", description = "게시글에 댓글을 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "댓글 추가 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @PostMapping("/share/{boardId}/comments")
    public ResponseEntity<?> addComment(Principal principal, @PathVariable Long boardId, @RequestBody CommentDTO commentDTO) {
        Long memberId = Long.parseLong(principal.getName());
        try {
            commentService.addComment(boardId, memberId, commentDTO.getContent());
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            log.error("댓글 추가 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("댓글 추가 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "댓글 삭제", description = "특정 게시판의 댓글을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
    @DeleteMapping("/share/{boardId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(Principal principal, @PathVariable Long boardId, @PathVariable Long commentId) {
        Long memberId = Long.parseLong(principal.getName());
        try {
            commentService.deleteComment(boardId, commentId, memberId);
            return ResponseEntity.ok().body("댓글이 성공적으로 삭제되었습니다.");
        } catch (ResourceNotFoundException e) {
            log.error("댓글 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 댓글을 찾을 수 없습니다.");
        } catch (AccessDeniedException e) {
            log.error("댓글 삭제 권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("삭제 권한이 없습니다.");
        } catch (Exception e) {
            log.error("댓글 삭제 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("댓글 삭제 중 오류가 발생했습니다.");
        }
    }
}