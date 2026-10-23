package btl.vexemphim.booking.repository;

import btl.vexemphim.booking.entity.StaffNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffNoticeRepository extends JpaRepository<StaffNotice, Long> {
    List<StaffNotice> findTop30ByAudienceOrderByCreatedAtDesc(String audience);
}
