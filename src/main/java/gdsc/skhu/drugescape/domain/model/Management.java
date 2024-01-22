package gdsc.skhu.drugescape.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Management {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int stopDrug;

    @Column(nullable = false)
    private int exercise;

    @Column(nullable = false)
    private int meal;

    @Column(nullable = false)
    private int medication;

    @Column(nullable = false)
    private LocalDate lastManagedDate; // 마지막 관리 기록 날짜 필드 추가

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;
}