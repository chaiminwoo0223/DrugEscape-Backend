package gdsc.skhu.drugescape.service;

import gdsc.skhu.drugescape.domain.model.Member;
import gdsc.skhu.drugescape.domain.model.Report;
import gdsc.skhu.drugescape.domain.repository.MemberRepository;
import gdsc.skhu.drugescape.domain.repository.ReportRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
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
    public Report modifyOrCreateReport(Long memberId, int point, int maximumDays, int dailyGoals) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));
        // reportId를 사용하지 않고, memberId를 기반으로 Report를 찾거나 생성합니다.
        return reportRepository.findByMemberId(memberId)
                .map(report -> {
                    if (!report.getMember().equals(member)) {
                        throw new AccessDeniedException("Unauthorized to update this report");
                    }
                    report.applyUpdates(point, maximumDays, dailyGoals);
                    return reportRepository.save(report);
                })
                .orElseGet(() -> {
                    Report newReport = Report.builder()
                            .member(member)
                            .point(point)
                            .maximumDays(maximumDays)
                            .dailyGoals(dailyGoals)
                            .build();
                    return reportRepository.save(newReport);
                });
    }
}