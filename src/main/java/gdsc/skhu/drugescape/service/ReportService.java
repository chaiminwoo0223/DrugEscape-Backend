package gdsc.skhu.drugescape.service;

import gdsc.skhu.drugescape.domain.model.Report;
import gdsc.skhu.drugescape.domain.repository.ReportRepository;
import org.springframework.stereotype.Service;

@Service
public class ReportService {
    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public Report updateReport(Long reportId, int point, int accumulatedDays, int maximumDays, int dailyGoals) {
        return reportRepository.findById(reportId)
                .map(report -> {
                    report.updateReport(point, accumulatedDays, maximumDays, dailyGoals);
                    return reportRepository.save(report);
                })
                .orElseGet(() -> {
                    Report newReport = Report.builder()
                            .point(point)
                            .accumulatedDays(accumulatedDays)
                            .maximumDays(maximumDays)
                            .dailyGoals(dailyGoals)
                            .build();
                    return reportRepository.save(newReport);
                });
    }
}