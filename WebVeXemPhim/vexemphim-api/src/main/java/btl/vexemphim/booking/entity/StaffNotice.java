package btl.vexemphim.booking.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "staff_notices", indexes = @Index(name = "idx_notice_audience_created", columnList = "audience, created_at"))
public class StaffNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ADMIN hoặc CASHIER. */
    @Column(nullable = false, length = 16)
    private String audience;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 1000)
    private String body;

    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
