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
    @JoinColumn(name = "report_id", nullable = false)
    private Report report; // null 값이 없도록 수정해야 한다.

    public void completeIndividualDonation() {
        this.donatedPoint += this.donatingPoint;
        this.donatingPoint = 0;
    }
}