package gdsc.skhu.drugescape.service;

import gdsc.skhu.drugescape.domain.dto.ReportDTO;
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
    public Report createReport(Long memberId, ReportDTO reportDTO) {
        Member member = findMemberById(memberId);
        Report newReport = Report.builder()
                .member(member)
                .point(reportDTO.getPoint())
                .maximumDays(reportDTO.getMaximumDays())
                .dailyGoals(reportDTO.getDailyGoals())
                .build();
        return reportRepository.save(newReport);
    }

    @Transactional
    public Report modifyReport(Long memberId, int point, int maximumDays, int dailyGoals) {
        Member member = findMemberById(memberId);
        Report report = findReportByMember(member);
        report.applyUpdates(point, maximumDays, dailyGoals);
        return reportRepository.save(report);
    }

    @Transactional
    public Report getReport(Long memberId) {
        Member member = findMemberById(memberId);
        return findReportByMember(member);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));
    }

    private Report findReportByMember(Member member) {
        return reportRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new EntityNotFoundException("Report not found for member id: " + member.getId()));
    }
}