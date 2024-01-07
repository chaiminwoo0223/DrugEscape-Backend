package gdsc.skhu.drugescape.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseErrorDTO {
    @Schema(description = "오류 메시지")
    private final String message;

    @Schema(description = "HTTP 상태 코드")
    private final int status;
}