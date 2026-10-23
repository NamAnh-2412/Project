package btl.vexemphim.booking.repository;

import btl.vexemphim.booking.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStatusAndPaidAtGreaterThanEqualAndPaidAtLessThan(
            String status, LocalDateTime from, LocalDateTime to);

    Optional<Payment> findByOrderCode(Long orderCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Payment> findByGatewayOrderId(String gatewayOrderId);

    Optional<Payment> findFirstByReservationIdAndMethodAndStatusOrderByIdDesc(
            Long reservationId, String method, String status);

    Optional<Payment> findFirstByReservationIdAndStatusOrderByIdDesc(Long reservationId, String status);

    List<Payment> findByReservationIdIn(Collection<Long> reservationIds);
}
