package gdsc.skhu.drugescape.controller;

import gdsc.skhu.drugescape.domain.dto.DonationDTO;
import gdsc.skhu.drugescape.domain.dto.ResponseErrorDTO;
import gdsc.skhu.drugescape.service.DonationService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/drugescape")
@RestController
@RequiredArgsConstructor
@Tag(name = "Donation API", description = "기부 처리 관련 API")
public class DonationController {
    private final DonationService donationService;

    @Operation(summary = "Donated 버튼 클릭", description = "사용자가 기부 버튼을 클릭하면 이 메서드가 호출됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기부 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "보고서를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class)))
    })
    @PostMapping("/donate")
    public ResponseEntity<String> donate(@RequestParam Long reportId, @RequestBody DonationDTO donationDTO) {
        donationService.recordDonation(reportId, donationDTO.getDonatingPoint());
        return ResponseEntity.ok("기부가 성공적으로 처리되었습니다.");
    }

    @Operation(summary = "기부 완료", description = "지정된 보고서의 모든 기부를 완료합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기부 최종화 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "보고서를 찾을 수 없음", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class)))
    })
    @PostMapping("/donate/finalize")
    public ResponseEntity<String> finalizeDonations(@RequestParam Long reportId) {
        donationService.finalizeAllDonationsForReport(reportId);
        return ResponseEntity.ok("ID가 " + reportId + "인 보고서에 대한 모든 기부가 최종적으로 완료되었습니다.");
    }
}