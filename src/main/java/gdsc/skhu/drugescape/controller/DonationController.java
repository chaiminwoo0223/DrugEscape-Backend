package gdsc.skhu.drugescape.controller;

import gdsc.skhu.drugescape.domain.dto.DonationDTO;
import gdsc.skhu.drugescape.service.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/drugescape")
@RequiredArgsConstructor
public class DonationController {
    private final DonationService donationService;

    @PostMapping("/donate") // Post 방식은 OK, Donated 버튼 클릭
    public ResponseEntity<String> donate(@RequestParam Long reportId, @RequestBody DonationDTO donationDTO) {
        donationService.donation(reportId, donationDTO.getDonatingPoint());
        return ResponseEntity.ok("기부가 성공적으로 처리되었습니다.");
    }

    @PostMapping("/donate/clear") // Get or Post or Request 검토
    public ResponseEntity<String> finalizeDonations(@RequestParam Long reportId) {
        donationService.donationFinalization(reportId);
        return ResponseEntity.ok("모든 기부가 최종적으로 완료되었습니다.");
    }
}