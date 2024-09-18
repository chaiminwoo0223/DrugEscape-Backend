package gdsc.skhu.drugescape.service;

import gdsc.skhu.drugescape.domain.model.Report;
import gdsc.skhu.drugescape.domain.repository.ReportRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;

    @Transactional
    public Report getReport(Long memberId) {
        return reportRepository.findByMemberId(memberId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 ID에 대한 보고서를 찾을 수 없습니다: " + memberId));
    }
}