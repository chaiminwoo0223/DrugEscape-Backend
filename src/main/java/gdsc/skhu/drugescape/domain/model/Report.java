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

    @Column(nullable = false)
    private int point;

    @Column(nullable = false)
    private int accumulatedDays;

    @Column(nullable = false)
    private int maximumDays;

    @Column(nullable = false)
    private int dailyGoals;

    @OneToMany(mappedBy = "report", fetch = FetchType.LAZY)
    private List<Donation> donations;

    public void updateReport(int point, int accumulatedDays, int maximumDays, int dailyGoals) {
        this.point = point;
        this.accumulatedDays = accumulatedDays;
        this.maximumDays = Math.max(this.maximumDays, maximumDays);
        this.dailyGoals = dailyGoals;
    }

    public void pointDecrease(int pointsToDecrease) {
        if (this.point < pointsToDecrease) {
            throw new IllegalStateException("기부할 수 있는 포인트가 부족합니다.");
        }
        this.point -= pointsToDecrease;
    }
}