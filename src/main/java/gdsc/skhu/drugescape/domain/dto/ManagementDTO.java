package gdsc.skhu.drugescape.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ManagementDTO {
    @Schema(description = "단약 기록", example = "Yes or No")
    private int stopDrug;

    @Schema(description = "운동 기록", example = "Yes or No")
    private int exercise;

    @Schema(description = "식사 기록", example = "breakfast, lunch, dinner")
    private int meal;

    @Schema(description = "치료약 섭취 기록", example = "morning, lunch, evening, none")
    private int medication;
}
