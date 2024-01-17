package gdsc.skhu.drugescape.service;

import gdsc.skhu.drugescape.domain.model.Donation;
import gdsc.skhu.drugescape.domain.model.Report;
import gdsc.skhu.drugescape.domain.repository.DonationRepository;
import gdsc.skhu.drugescape.domain.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DonationService {
    private final DonationRepository donationRepository;
    private final ReportRepository reportRepository;

    @Transactional
    public int createDonation(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report with ID " + reportId + " not found."));
        Donation.builder()
                .donatingPoint(report.getPoint())
                .report(report)
                .build();
        return report.getPoint(); // Report의 현재 포인트 반환
    }

    @Transactional
    public void recordDonation(Long reportId, int donatingPoint) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalStateException("Report not found."));
        if (report.getPoint() < donatingPoint) {
            throw new IllegalStateException("There are not enough points to donate.");
        }
        report.pointDecrease(donatingPoint);
        Donation donation = Donation.builder()
                .donatingPoint(donatingPoint)
                .report(report)
                .build();
        donationRepository.save(donation);
    }

    @Transactional
    public void completeDonations() {
        List<Donation> donations = donationRepository.findAll();
        donations.forEach(Donation::completeIndividualDonation);
        donationRepository.saveAll(donations);
    }
}