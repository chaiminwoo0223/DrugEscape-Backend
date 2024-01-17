package gdsc.skhu.drugescape.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DonationDTO {
    @Schema(description = "기부할 포인트", example = "15000")
    private int donatingPoint;
}