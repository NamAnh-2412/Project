package btl.vexemphim.booking.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pos_shifts")
@EntityListeners(AuditingEntityListener.class)
public class PosShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cashier_user_id", nullable = false)
    private Long cashierUserId;

    /** OPEN | CLOSED */
    @Column(nullable = false, length = 16)
    private String status = "OPEN";

    @Column(name = "opening_cash", precision = 12, scale = 2)
    private BigDecimal openingCash = BigDecimal.ZERO;

    @Column(name = "cash_sales", precision = 12, scale = 2)
    private BigDecimal cashSales = BigDecimal.ZERO;

    @Column(name = "card_sales", precision = 12, scale = 2)
    private BigDecimal cardSales = BigDecimal.ZERO;

    @Column(name = "snack_sales", precision = 12, scale = 2)
    private BigDecimal snackSales = BigDecimal.ZERO;

    @Column(name = "ticket_count")
    private int ticketCount;

    @Column(name = "expected_cash", precision = 12, scale = 2)
    private BigDecimal expectedCash;

    @Column(name = "actual_cash", precision = 12, scale = 2)
    private BigDecimal actualCash;

    @Column(precision = 12, scale = 2)
    private BigDecimal difference;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public Long getCashierUserId() { return cashierUserId; }
    public void setCashierUserId(Long cashierUserId) { this.cashierUserId = cashierUserId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getOpeningCash() { return openingCash; }
    public void setOpeningCash(BigDecimal openingCash) { this.openingCash = openingCash; }
    public BigDecimal getCashSales() { return cashSales; }
    public void setCashSales(BigDecimal cashSales) { this.cashSales = cashSales; }
    public BigDecimal getCardSales() { return cardSales; }
    public void setCardSales(BigDecimal cardSales) { this.cardSales = cardSales; }
    public BigDecimal getSnackSales() { return snackSales; }
    public void setSnackSales(BigDecimal snackSales) { this.snackSales = snackSales; }
    public int getTicketCount() { return ticketCount; }
    public void setTicketCount(int ticketCount) { this.ticketCount = ticketCount; }
    public BigDecimal getExpectedCash() { return expectedCash; }
    public void setExpectedCash(BigDecimal expectedCash) { this.expectedCash = expectedCash; }
    public BigDecimal getActualCash() { return actualCash; }
    public void setActualCash(BigDecimal actualCash) { this.actualCash = actualCash; }
    public BigDecimal getDifference() { return difference; }
    public void setDifference(BigDecimal difference) { this.difference = difference; }
    public LocalDateTime getOpenedAt() { return openedAt; }
    public void setOpenedAt(LocalDateTime openedAt) { this.openedAt = openedAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
}
