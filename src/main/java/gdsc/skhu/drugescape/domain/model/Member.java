package gdsc.skhu.drugescape.domain.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String picture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Report> reports;

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    private List<Board> boards;     // 작성글

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    private List<Comment> comments; // 댓글

    @OneToMany(mappedBy = "member", orphanRemoval = true)
    private List<Heart> hearts;       // 유저가 누른 좋아요

    public void changeToAdmin() {
        this.role = Role.ROLE_ADMIN;
    }
}