package btl.vexemphim.booking.repository;

import btl.vexemphim.booking.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByMovieIdOrderByCreatedAtDesc(Long movieId);

    Optional<Review> findByUserIdAndMovieId(Long userId, Long movieId);
}
