package gdsc.skhu.drugescape.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {
    @Schema(description = "포인트")
    private int point;

    @Schema(description = "최대 단약일")
    private int maximumDays;

    @Schema(description = "하루 성취율")
    private int dailyGoals;
}