package gdsc.skhu.drugescape.controller;

import gdsc.skhu.drugescape.domain.dto.ResponseErrorDTO;
import gdsc.skhu.drugescape.domain.model.Report;
import gdsc.skhu.drugescape.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/drugescape")
@RequiredArgsConstructor
@Tag(name = "Report API", description = "보고서 관련 API")
public class ReportController {
    private final ReportService reportService;

    @Operation(summary = "보고서 조회", description = "현재 사용자의 보고서를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "보고서 조회 성공"),
            @ApiResponse(responseCode = "404", description = "보고서를 찾을 수 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/report")
    public ResponseEntity<?> report(Principal principal) {
        try {
            Long memberId = Long.parseLong(principal.getName());
            Report report = reportService.getReport(memberId);
            return ResponseEntity.ok(report);
        } catch (NumberFormatException e) {
            log.error("잘못된 사용자 ID 형식: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (EntityNotFoundException e) {
            log.error("보고서를 찾을 수 없음: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("보고서 조회 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseErrorDTO("서버 내부 오류", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}