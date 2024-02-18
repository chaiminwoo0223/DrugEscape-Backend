package gdsc.skhu.drugescape.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TokenDTO {
    @Schema(description = "액세스 토큰")
    private String accessToken;

    @Schema(description = "리프레시 토큰")
    private String refreshToken;
}