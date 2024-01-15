package gdsc.skhu.drugescape.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int donatingPoint;

    @Column(nullable = false)
    private int donatedPoint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportId", nullable = false) // "report_id" 컬럼과 맵핑
    private Report report;

    public void completeIndividualDonation() {
        this.donatedPoint += this.donatingPoint;
        this.donatingPoint = 0;
    }
}