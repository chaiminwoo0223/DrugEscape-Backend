package gdsc.skhu.drugescape.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "point", nullable = false)
    private int point;

    @Column(name = "accumulated_days", nullable = false)
    private int accumulatedDays;

    @Column(name = "maximum_days", nullable = false)
    private int maximumDays;

    @Column(name = "daily_goals", nullable = false)
    private int dailyGoals;

    @OneToMany(mappedBy = "report", fetch = FetchType.LAZY)
    private List<Donation> donations;

    public void pointDecrease(int pointsToDecrease) {
        if (this.point < pointsToDecrease) {
            throw new IllegalStateException("기부할 수 있는 포인트가 부족합니다.");
        }
        this.point -= pointsToDecrease;
    }
}