package gdsc.skhu.drugescape.controller;

import gdsc.skhu.drugescape.domain.dto.ReportDTO;
import gdsc.skhu.drugescape.domain.dto.ResponseErrorDTO;
import gdsc.skhu.drugescape.domain.model.Report;
import gdsc.skhu.drugescape.service.ReportService;
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
@RestController
@RequestMapping("/drugescape")
@RequiredArgsConstructor
@Tag(name = "Report API", description = "보고서 관련 API")
public class ReportController {
    private final ReportService reportService;

    @Operation(summary = "보고서 업데이트", description = "사용자의 보고서를 업데이트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보고서 업데이트 성공"),
            @ApiResponse(responseCode = "404", description = "보고서를 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class)))
    })
    @PutMapping("/report/{reportId}")
    public ResponseEntity<ReportDTO> updateReport(@PathVariable Long reportId,
                                                  @RequestBody ReportDTO reportDTO) {
        Report updatedReport = reportService.updateReport(reportId, reportDTO.getPoint(), reportDTO.getAccumulatedDays(), reportDTO.getMaximumDays(), reportDTO.getDailyGoals());
        ReportDTO updatedReportDTO = ReportDTO.builder()
                .point(updatedReport.getPoint())
                .accumulatedDays(updatedReport.getAccumulatedDays())
                .maximumDays(updatedReport.getMaximumDays())
                .dailyGoals(updatedReport.getDailyGoals())
                .build();
        return ResponseEntity.ok(updatedReportDTO);
    }
}