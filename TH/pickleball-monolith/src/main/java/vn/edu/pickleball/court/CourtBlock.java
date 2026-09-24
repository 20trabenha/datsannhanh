package vn.edu.pickleball.court;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "court_blocks")
public class CourtBlock {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Court court;
    @Column(nullable = false)
    private LocalDateTime startAt;
    @Column(nullable = false)
    private LocalDateTime endAt;
    @Column(nullable = false, length = 300)
    private String reason;
    protected CourtBlock() {}
    public CourtBlock(Court court, LocalDateTime startAt, LocalDateTime endAt, String reason) {
        this.court = court; this.startAt = startAt; this.endAt = endAt; this.reason = reason;
    }
    public Long getId() { return id; }
    public Court getCourt() { return court; }
    public LocalDateTime getStartAt() { return startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public String getReason() { return reason; }
}
