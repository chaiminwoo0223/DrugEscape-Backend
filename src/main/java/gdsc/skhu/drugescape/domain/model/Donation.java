package gdsc.skhu.drugescape.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private int donatingPoint;

    @Column(nullable = false)
    private int donatedPoint;

    @Column(nullable = false)
    private Long reportId; // Report와의 연관 관계를 위한 필드

    public void finalizeIndividualDonation() {
        this.donatedPoint += this.donatingPoint; // 기부된 포인트로 추가
        this.donatingPoint = 0; // 기부 처리 후, donatingPoint 초기화
    }
}