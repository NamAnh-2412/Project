package btl.vexemphim.booking.repository;

import btl.vexemphim.booking.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCode(String code);

    @Modifying(clearAutomatically = false, flushAutomatically = true)
    @Query("update Voucher v set v.usedCount = v.usedCount + 1 where v.id = :id and v.usedCount < v.usageLimit")
    int consumeIfAvailable(@Param("id") Long id);
}
