package btl.vexemphim.booking.repository;

import btl.vexemphim.booking.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByTicketCode(String ticketCode);

    @Query("""
            select count(t) from Ticket t
            where t.status = 'CHECKED_IN'
              and t.reservation.customerUserId = :userId
              and t.reservation.movieId = :movieId
            """)
    long countCheckedInForMovie(@Param("userId") Long userId, @Param("movieId") Long movieId);
}
