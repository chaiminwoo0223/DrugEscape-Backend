package gdsc.skhu.drugescape.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ResponseErrorDTO(
        @Schema(description = "오류 메시지")
        String message,

        @Schema(description = "HTTP 상태 코드")
        int status
) {}