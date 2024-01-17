package gdsc.skhu.drugescape.service;

import gdsc.skhu.drugescape.domain.model.Member;
import gdsc.skhu.drugescape.domain.model.Report;
import gdsc.skhu.drugescape.domain.repository.MemberRepository;
import gdsc.skhu.drugescape.domain.repository.ReportRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;

    public ReportService(ReportRepository reportRepository, MemberRepository memberRepository) {
        this.reportRepository = reportRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Report modifyOrCreateReport(Long memberId, Long reportId, int point, int accumulatedDays, int maximumDays, int dailyGoals) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));
        return reportRepository.findById(reportId)
                .filter(report -> report.getMember().equals(member))
                .map(report -> {
                    report.applyUpdates(point, accumulatedDays, maximumDays, dailyGoals);
                    return reportRepository.save(report);
                })
                .orElseGet(() -> {
                    Report newReport = Report.builder()
                            .member(member)
                            .point(point)
                            .accumulatedDays(accumulatedDays)
                            .maximumDays(maximumDays)
                            .dailyGoals(dailyGoals)
                            .build();
                    return reportRepository.save(newReport);
                });
    }
}