package gdsc.skhu.drugescape.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Management { // report를 작성하기 위해, 임의로 지은 것이다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int stopDrug; // Yes or No

    @Column(nullable = false)
    private int exercise; // Yes or No

    @Column(nullable = false)
    private int meal; // breakfast, lunch, dinner

    @Column(nullable = false)
    private int medication; // morning, lunch, evening, none

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reportId", nullable = false) // "report_id" 컬럼과 맵핑
    private Report report;
}
