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
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("ID가 있는 사용자를 찾을 수 없습니다: " + memberId));
        Report newReport = Report.builder()
                .member(member)
                .point(reportDTO.getPoint())
                .maximumDays(reportDTO.getMaximumDays())
                .dailyGoals(reportDTO.getDailyGoals())
                .build();
        return reportRepository.save(newReport);
    }

    @Transactional
    public Report getReport(Long memberId) {
        return reportRepository.findByMemberId(memberId)
                .orElseThrow(() -> new EntityNotFoundException("사용자 ID에 대한 보고서를 찾을 수 없습니다: " + memberId));
    }
}