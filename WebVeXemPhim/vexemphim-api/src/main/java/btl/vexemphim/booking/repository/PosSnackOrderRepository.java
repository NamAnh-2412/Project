package btl.vexemphim.booking.repository;

import btl.vexemphim.booking.entity.PosSnackOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PosSnackOrderRepository extends JpaRepository<PosSnackOrder, Long> {
    List<PosSnackOrder> findByCashierUserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            Long cashierUserId, LocalDateTime from, LocalDateTime to);
}
