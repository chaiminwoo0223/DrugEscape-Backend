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
public class Report {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private int point;

    @Column(unique = true, nullable = false)
    private int accumulatedDays;

    @Column(unique = true, nullable = false)
    private int maximumDays;

    @Column(unique = true, nullable = false)
    private int dailyGoals;

    @Column(nullable = false)
    private int donatingPoint;

    public void pointDecrease(int pointsToDecrease) {
        if (this.point < pointsToDecrease) {
            throw new IllegalStateException("기부할 수 있는 포인트가 부족합니다.");
        }
        this.point -= pointsToDecrease;
    }

    public void donationProcess() {
        if (this.donatingPoint > this.point) {
            throw new IllegalStateException("기부 가능한 포인트보다 더 많은 포인트를 기부하려고 합니다.");
        }
        this.point -= this.donatingPoint;
    }
}