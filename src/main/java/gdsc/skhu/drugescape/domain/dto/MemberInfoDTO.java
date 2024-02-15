package gdsc.skhu.drugescape.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfoDTO {
    @Schema(description = "이름")
    private String name;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "사진")
    private String picture;

    @Schema(description = "총 기부된 포인트")
    private int totalDonatedPoints;
}