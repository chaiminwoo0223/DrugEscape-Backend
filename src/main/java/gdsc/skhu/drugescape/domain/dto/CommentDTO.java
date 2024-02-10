package gdsc.skhu.drugescape.domain.dto;

import gdsc.skhu.drugescape.domain.model.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    @Schema(description = "내용", example = "화이팅")
    private String content;

    @Schema(description = "작성자", example = "사용자명")
    private String memberName; // 댓글 작성자 이름 추가

    // Comment 엔터티로부터 CommentDTO 생성하는 메소드에 작성자 이름을 설정하는 로직 추가
    public static CommentDTO from(Comment comment) {
        return CommentDTO.builder()
                .content(comment.getContent())
                .memberName(comment.getMember().getName()) // 작성자 이름 설정
                .build();
    }
}