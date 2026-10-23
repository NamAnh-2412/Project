package btl.vexemphim.booking.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations", indexes = {
        @Index(name = "idx_res_status_expires", columnList = "status, hold_expires_at"),
        @Index(name = "idx_res_st_cust_status", columnList = "showtime_id, customer_user_id, status"),
        @Index(name = "idx_res_st_cash_status", columnList = "showtime_id, cashier_user_id, status"),
        @Index(name = "idx_res_cust_status_created", columnList = "customer_user_id, status, created_at")
})
@EntityListeners(AuditingEntityListener.class)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "showtime_id", nullable = false)
    private Long showtimeId;

    @Column(name = "movie_id")
    private Long movieId;

    @Column(name = "movie_title")
    private String movieTitle;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "room_name")
    private String roomName;

    @Column(nullable = false, length = 16)
    private String channel;

    @Column(name = "customer_user_id")
    private Long customerUserId;

    @Column(name = "cashier_user_id")
    private Long cashierUserId;

    @Column(name = "shift_id")
    private Long shiftId;

    @Column(name = "guest_name", length = 128)
    private String guestName;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "hold_expires_at")
    private LocalDateTime holdExpiresAt;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "voucher_id")
    private Long voucherId;

    @Column(name = "voucher_code", length = 32)
    private String voucherCode;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationSeat> seats = new ArrayList<>();

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets = new ArrayList<>();

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationConcession> concessions = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getShowtimeId() { return showtimeId; }
    public void setShowtimeId(Long showtimeId) { this.showtimeId = showtimeId; }
    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public Long getCustomerUserId() { return customerUserId; }
    public void setCustomerUserId(Long customerUserId) { this.customerUserId = customerUserId; }
    public Long getCashierUserId() { return cashierUserId; }
    public void setCashierUserId(Long cashierUserId) { this.cashierUserId = cashierUserId; }
    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getHoldExpiresAt() { return holdExpiresAt; }
    public void setHoldExpiresAt(LocalDateTime holdExpiresAt) { this.holdExpiresAt = holdExpiresAt; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public Long getVoucherId() { return voucherId; }
    public void setVoucherId(Long voucherId) { this.voucherId = voucherId; }
    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<ReservationSeat> getSeats() { return seats; }
    public void setSeats(List<ReservationSeat> seats) { this.seats = seats; }
    public List<Ticket> getTickets() { return tickets; }
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }
    public List<ReservationConcession> getConcessions() { return concessions; }
    public void setConcessions(List<ReservationConcession> concessions) { this.concessions = concessions; }
}
