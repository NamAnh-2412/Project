package btl.vexemphim.booking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@EntityListeners(AuditingEntityListener.class)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reservation_id")
    @JsonIgnore
    private Reservation reservation;

    @Column(name = "ticket_code", unique = true, length = 32)
    private String ticketCode;

    @Column(name = "qr_payload", length = 128)
    private String qrPayload;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt = LocalDateTime.now();

    /** ISSUED after pay; CHECKED_IN after door scan. Null on old rows = ISSUED. */
    @Column(length = 16)
    private String status = "ISSUED";

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Reservation getReservation() { return reservation; }
    public void setReservation(Reservation reservation) { this.reservation = reservation; }
    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }
    public String getQrPayload() { return qrPayload; }
    public void setQrPayload(String qrPayload) { this.qrPayload = qrPayload; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCheckedInAt() { return checkedInAt; }
    public void setCheckedInAt(LocalDateTime checkedInAt) { this.checkedInAt = checkedInAt; }
}
