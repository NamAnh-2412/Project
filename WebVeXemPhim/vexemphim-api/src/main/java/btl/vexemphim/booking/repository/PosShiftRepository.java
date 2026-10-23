package btl.vexemphim.booking.repository;

import btl.vexemphim.booking.entity.PosShift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PosShiftRepository extends JpaRepository<PosShift, Long> {
    Optional<PosShift> findFirstByCashierUserIdAndStatusOrderByIdDesc(Long cashierUserId, String status);
}
