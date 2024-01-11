package gdsc.skhu.drugescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import gdsc.skhu.drugescape.domain.model.Donation;
import gdsc.skhu.drugescape.domain.model.Report;
import gdsc.skhu.drugescape.domain.repository.DonationRepository;
import gdsc.skhu.drugescape.domain.repository.ReportRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DonationService {
    private final DonationRepository donationRepository;
    private final ReportRepository reportRepository;

    @Transactional
    public void donation(Long reportId, int point) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalStateException("ID가 " + reportId + "인 보고서를 찾을 수 없습니다."));
        report.pointDecrease(point); // 포인트 감소
        reportRepository.save(report);
        Donation donation = Donation.builder()
                .donatedPoint(point)
                .reportId(reportId)
                .build();
        donationRepository.save(donation);
    }

    @Transactional
    public void donationFinalization(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalStateException("ID가 " + reportId + "인 보고서를 찾을 수 없습니다."));
        report.donationProcess(); // 기부 처리
        reportRepository.save(report);
        List<Donation> donations = donationRepository.findByReportId(reportId);
        donations.forEach(donation -> {
            donation.donationFinalization();
            donationRepository.save(donation);
        });
    }
}