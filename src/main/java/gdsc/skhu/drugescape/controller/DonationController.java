package gdsc.skhu.drugescape.controller;

import gdsc.skhu.drugescape.domain.dto.DonationDTO;
import gdsc.skhu.drugescape.domain.dto.ResponseErrorDTO;
import gdsc.skhu.drugescape.service.DonationService;
import gdsc.skhu.drugescape.service.MemberService;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/drugescape")
@RequiredArgsConstructor
@Tag(name = "Donation API", description = "기부 관련 API")
public class DonationController {
    private final DonationService donationService;
    private final MemberService memberService;

    @Operation(summary = "기부 포인트 조회", description = "사용자가 기부할 수 있는 현재 포인트를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "보고서를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class)))
    })
    @GetMapping("/donate")
    public ResponseEntity<Integer> getAvailableDonationPoints(Principal principal) {
        try {
            Long memberId = Long.parseLong(principal.getName());
            int availablePoints = donationService.getAvailablePointsForDonation(memberId);
            return ResponseEntity.ok(availablePoints);
        } catch (NumberFormatException e) {
            log.error("기부 포인트 조회 중 사용자 ID 형식 오류", e);
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException e) {
            log.error("기부 포인트 조회 중 찾을 수 없는 회원 ID - 사용자 ID: {}", principal.getName(), e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("기부 포인트 조회 중 예기치 않은 오류 발생 - 사용자 ID: {}", principal.getName(), e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @Operation(summary = "Donated 버튼 클릭", description = "사용자가 기부 버튼을 클릭하면 이 메서드가 호출됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기부 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "보고서를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class)))
    })
    @PostMapping("/donate")
    public ResponseEntity<String> donate(Principal principal, @RequestBody DonationDTO donationDTO) {
        try {
            Long memberId = Long.parseLong(principal.getName());
            donationService.processDonation(memberId, donationDTO);
            return ResponseEntity.ok("기부가 성공적으로 처리되었습니다.");
        } catch (NumberFormatException e) {
            log.error("잘못된 회원 ID 형식 - 회원 이름: {}", principal.getName(), e);
            return ResponseEntity.badRequest().body("잘못된 회원 ID 형식입니다.");
        } catch (IllegalStateException e) {
            log.error("기부 처리 중 오류 발생 - 오류 메시지: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("기부 처리 중 예기치 않은 오류 발생", e);
            return ResponseEntity.internalServerError().body("기부 처리 중 오류 발생");
        }
    }

    // 추후, 회의를 통해 제거 or 변화
    @Operation(summary = "총 기부 포인트 조회", description = "지금까지 모인 기부 포인트를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공 - 총 기부된 포인트의 합계를 반환합니다."),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 - 로그인이 필요합니다.", content = @Content),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류 - 요청 처리 중 예기치 못한 오류가 발생했습니다.", content = @Content)
    })
    @GetMapping("/donate/total")
    public ResponseEntity<Integer> getTotalDonatedPoints(Principal principal) {
        memberService.upgradeToAdminRole(principal);
        try {
            int totalDonatedPoints = donationService.getTotalDonatedPoints();
            return ResponseEntity.ok(totalDonatedPoints);
        } catch (Exception e) {
            log.error("총 기부 포인트 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}