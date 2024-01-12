package gdsc.skhu.drugescape.controller;

import gdsc.skhu.drugescape.domain.dto.ResponseErrorDTO;
import gdsc.skhu.drugescape.domain.model.Donation;
import gdsc.skhu.drugescape.service.DonationService;
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

@Slf4j
@RestController
@RequestMapping("/drugescape")
@RequiredArgsConstructor
@Tag(name = "Donation API", description = "기부 관련 API")
public class DonationController {
    private final DonationService donationService;

    @Operation(summary = "기부 가능 포인트 조회", description = "사용자가 기부할 수 있는 현재 포인트를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/donate")
    public ResponseEntity<Integer> getAvailableDonationPoints(@RequestParam Long reportId) {
        log.info("기부 가능 포인트 조회 요청 - 보고서 ID: {}", reportId);
        try {
            int availablePoints = donationService.createDonation(reportId);
            return ResponseEntity.ok(availablePoints);
        } catch (Exception e) {
            log.error("기부 가능 포인트 조회 중 예기치 않은 오류 발생 - 보고서 ID: {}", reportId, e);
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
    public ResponseEntity<String> donate(@RequestParam Long reportId, @RequestParam int donatingPoint) {
        try {
            log.info("기부 처리 시작 - 보고서 ID: {}, 기부 포인트: {}", reportId, donatingPoint);
            donationService.recordDonation(reportId, donatingPoint);
            return ResponseEntity.ok("기부가 성공적으로 처리되었습니다.");
        } catch (IllegalStateException e) {
            log.error("기부 처리 중 오류 발생 - 보고서 ID: {}, 오류 메시지: {}", reportId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("기부 처리 중 예기치 않은 오류 발생 - 보고서 ID: {}", reportId, e);
            return ResponseEntity.internalServerError().body("기부 처리 중 오류 발생");
        }
    }

    @Operation(summary = "모든 기부 완료", description = "시스템에 기록된 모든 기부 내역을 최종적으로 완료합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "모든 기부 완료 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class)))
    })
    @PostMapping("/donate/complete")
    public ResponseEntity<String> completeAllDonations() {
        try {
            log.info("모든 기부 완료 처리 시작");
            donationService.completeDonations();
            return ResponseEntity.ok("모든 기부가 최종적으로 완료되었습니다.");
        } catch (Exception e) {
            log.error("기부 완료 처리 중 예기치 않은 오류 발생", e);
            return ResponseEntity.internalServerError().body("기부 완료 중 오류 발생");
        }
    }

    @Operation(summary = "총 기부 포인트 조회", description = "지금까지 모인 기부 포인트를 확인합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping("/donate/total")
    public ResponseEntity<Integer> getTotalDonatedPoints() {
        log.info("총 기부된 포인트 조회 요청");
        return ResponseEntity.ok(Donation.getTotalDonatedPoints());
    }
}