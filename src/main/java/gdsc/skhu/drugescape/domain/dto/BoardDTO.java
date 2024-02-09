package gdsc.skhu.drugescape.domain.dto;

import gdsc.skhu.drugescape.domain.model.Board;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BoardDTO {
    @Schema(description = "제목", example = "처음 글 씁니다.")
    private String title;

    @Schema(description = "내용", example = "저는 ~~~")
    private String content;

    @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
    private String imageURL; // 변경된 부분

    @Schema(description = "좋아요 수", example = "+10")
    private Integer heartCnt;

    @Schema(description = "만든 날짜", example = "2024.02.02")
    private LocalDateTime createdAt;

    @Schema(description = "수정한 날짜", example = "2024.02.03")
    private LocalDateTime lastModifiedAt;

    public static BoardDTO of(Board board) {
        return BoardDTO.builder()
                .title(board.getTitle())
                .content(board.getContent())
                .heartCnt(board.getHeartCnt())
                .createdAt(board.getCreatedAt())
                .lastModifiedAt(board.getLastModifiedAt())
                .build();
    }
}