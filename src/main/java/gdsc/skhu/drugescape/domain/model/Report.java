package gdsc.skhu.drugescape.domain.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder(toBuilder = true)
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
    private int maximumDays;

    @Column(nullable = false)
    private int dailyGoals;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonBackReference
    private Member member;

    public void pointDecrease(int pointsToDecrease) {
        if (this.point < pointsToDecrease) {
            throw new IllegalStateException("기부할 수 있는 포인트가 부족합니다.");
        }
        this.point -= pointsToDecrease;
    }
}