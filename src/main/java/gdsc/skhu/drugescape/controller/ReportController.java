package gdsc.skhu.drugescape.controller;

import gdsc.skhu.drugescape.domain.dto.ReportDTO;
import gdsc.skhu.drugescape.domain.dto.ResponseErrorDTO;
import gdsc.skhu.drugescape.domain.model.Member;
import gdsc.skhu.drugescape.domain.model.Report;
import gdsc.skhu.drugescape.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/drugescape")
@RequiredArgsConstructor
@Tag(name = "Report API", description = "보고서 관련 API")
public class ReportController {
    private final ReportService reportService;

    @Operation(summary = "보고서 업데이트", description = "기존 보고서의 정보를 업데이트합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보고서 업데이트 성공"),
            @ApiResponse(responseCode = "404", description = "보고서를 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class)))
    })
    @PutMapping("/report")
    public ResponseEntity<?> updateReport(@AuthenticationPrincipal Member currentMember,
                                          @RequestBody ReportDTO reportDTO) {
        try {
            Report updatedReport = reportService.modifyReport(currentMember.getId(), reportDTO.getPoint(), reportDTO.getMaximumDays(), reportDTO.getDailyGoals());
            return ResponseEntity.ok(updatedReport);
        } catch (Exception e) {
            log.error("보고서 업데이트 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseErrorDTO("서버 내부 오류", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @Operation(summary = "보고서 조회", description = "현재 사용자의 보고서를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보고서 조회 성공"),
            @ApiResponse(responseCode = "404", description = "보고서를 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/report")
    public ResponseEntity<?> getReport(@AuthenticationPrincipal Member currentMember) {
        try {
            Report report = reportService.getReport(currentMember.getId());
            return ResponseEntity.ok(report);
        } catch (EntityNotFoundException e) {
            log.error("보고서를 찾을 수 없음: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("보고서 조회 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseErrorDTO("서버 내부 오류", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @Operation(summary = "새 보고서 생성", description = "새로운 보고서를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "새 보고서 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ResponseErrorDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/report")
    public ResponseEntity<?> createReport(@AuthenticationPrincipal Member currentMember,
                                          @RequestBody ReportDTO reportDTO) {
        try {
            Report newReport = reportService.createReport(currentMember.getId(), reportDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(newReport);
        } catch (Exception e) {
            log.error("보고서 생성 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseErrorDTO("서버 내부 오류", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}