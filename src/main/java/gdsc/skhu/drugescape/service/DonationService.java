package gdsc.skhu.drugescape.service;

import gdsc.skhu.drugescape.domain.dto.DonationDTO;
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
    public int getAvailablePointsForDonation(Long memberId) {
        Report report = reportRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 ID가 " + memberId + "인 보고서를 찾을 수 없습니다."));
        return report.getPoint();
    }

    @Transactional
    public void processDonation(Long memberId, DonationDTO donationDTO) {
        Report report = reportRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalStateException("회원 ID가 " + memberId + "인 보고서를 찾을 수 없습니다."));
        if (report.getPoint() < donationDTO.getDonatingPoint()) {
            throw new IllegalStateException("기부할 포인트가 충분하지 않습니다.");
        }
        report.pointDecrease(donationDTO.getDonatingPoint());
        Donation donation = donationRepository.findByReportId(report.getId())
                .map(latestDonation -> latestDonation.toBuilder()
                        .donatedPoint(latestDonation.getDonatedPoint() + donationDTO.getDonatingPoint())
                        .build())
                .orElseGet(() -> Donation.builder()
                        .donatingPoint(donationDTO.getDonatingPoint())
                        .donatedPoint(donationDTO.getDonatingPoint())
                        .report(report)
                        .build());
        donationRepository.save(donation);
    }

    @Transactional(readOnly = true)
    public int getTotalDonatedPoints() {
        return donationRepository.findAll().stream()
                .mapToInt(Donation::getDonatedPoint)
                .sum();
    }
}