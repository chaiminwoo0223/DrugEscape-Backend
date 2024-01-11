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
    public void recordDonation(Long reportId, int donatingPoint) {
        Donation donation = Donation.builder()
                .donatingPoint(donatingPoint)
                .reportId(reportId)
                .build();
        donationRepository.save(donation);
    }

    @Transactional
    public void finalizeAllDonationsForReport(Long reportId) {
        List<Donation> donations = donationRepository.findByReportId(reportId);
        int totalDonatingPoint = donations.stream().mapToInt(Donation::getDonatingPoint).sum();
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalStateException("ID가 " + reportId + "인 보고서를 찾을 수 없습니다."));
        if (report.getPoint() < totalDonatingPoint) {
            throw new IllegalStateException("기부 가능한 포인트보다 더 많은 포인트를 기부하려고 합니다.");
        }
        report.pointDecrease(totalDonatingPoint); // 전체 기부 포인트 감소
        reportRepository.save(report);
        donations.forEach(donation -> {
            donation.finalizeIndividualDonation();
            donationRepository.save(donation);
        });
    }
}
