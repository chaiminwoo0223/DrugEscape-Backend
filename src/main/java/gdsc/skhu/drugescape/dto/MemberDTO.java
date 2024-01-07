package gdsc.skhu.drugescape.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    @Schema(description = "id")
    private String id;

    @Schema(description = "name")
    private String name;

    @Schema(description = "email")
    private String email;

    @Schema(description = "verified_email")
    private Boolean verifiedEmail;

    @Schema(description = "picture")
    private String picture;
}