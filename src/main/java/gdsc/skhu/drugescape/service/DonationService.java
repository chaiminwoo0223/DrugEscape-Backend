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
    public void recordDonation(Long reportId, int donatingPoint) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalStateException("보고서를 찾을 수 없습니다."));
        if (report.getPoint() < donatingPoint) {
            throw new IllegalStateException("기부할 수 있는 포인트가 부족합니다.");
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