package gdsc.skhu.drugescape.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ManagementDTO {
    @Schema(description = "단약 기록", example = "Yes, No")
    private int stopDrug;

    @Schema(description = "운동 기록", example = "Yes, No")
    private int exercise;

    @Schema(description = "식사 기록", example = "Breakfast, Lunch, Dinner")
    private int meal;

    @Schema(description = "치료약 섭취 기록", example = "Morning, Lunch, Evening, None")
    private int medication;
}