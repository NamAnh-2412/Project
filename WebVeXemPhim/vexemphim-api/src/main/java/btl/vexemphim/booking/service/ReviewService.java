package btl.vexemphim.booking.service;

import btl.vexemphim.booking.dto.ReviewRequest;
import btl.vexemphim.booking.dto.ReviewResponse;
import btl.vexemphim.booking.entity.Review;
import btl.vexemphim.booking.repository.ReviewRepository;
import btl.vexemphim.booking.repository.TicketRepository;
import btl.vexemphim.catalog.entity.Movie;
import btl.vexemphim.catalog.repository.MovieRepository;
import btl.vexemphim.common.exception.ApiException;
import btl.vexemphim.common.security.AuthContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final TicketRepository ticketRepository;
    private final MovieRepository movieRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         TicketRepository ticketRepository,
                         MovieRepository movieRepository) {
        this.reviewRepository = reviewRepository;
        this.ticketRepository = ticketRepository;
        this.movieRepository = movieRepository;
    }

    public List<ReviewResponse> list(Long movieId) {
        Long me = AuthContext.userId();
        return reviewRepository.findByMovieIdOrderByCreatedAtDesc(movieId).stream()
                .map(r -> toResponse(r, me != null && me.equals(r.getUserId())))
                .toList();
    }

    @Transactional
    public ReviewResponse save(Long movieId, ReviewRequest req) {
        if (!"CUSTOMER".equals(AuthContext.role())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Chỉ khách đã xem phim mới đánh giá");
        }
        Long userId = AuthContext.userId();
        if (ticketRepository.countCheckedInForMovie(userId, movieId) < 1) {
            throw new ApiException(HttpStatus.FORBIDDEN, "NOT_WATCHED", "Chỉ đánh giá sau khi vé đã được soát");
        }
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy phim"));
        Review review = reviewRepository.findByUserIdAndMovieId(userId, movieId).orElseGet(Review::new);
        review.setUserId(userId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        review.setUsername(auth == null ? "Khách" : auth.getName());
        review.setMovieId(movieId);
        review.setRating(req.rating());
        String comment = req.comment() == null ? "" : req.comment().trim();
        review.setComment(comment.isEmpty() ? null : comment);
        reviewRepository.save(review);
        refreshAverage(movie);
        return toResponse(review, true);
    }

    private void refreshAverage(Movie movie) {
        List<Review> all = reviewRepository.findByMovieIdOrderByCreatedAtDesc(movie.getId());
        if (all.isEmpty()) {
            movie.setRatingAverage(BigDecimal.ZERO);
            movie.setRatingCount(0);
            return;
        }
        int sum = all.stream().mapToInt(Review::getRating).sum();
        movie.setRatingCount(all.size());
        movie.setRatingAverage(BigDecimal.valueOf(sum)
                .divide(BigDecimal.valueOf(all.size()), 1, RoundingMode.HALF_UP));
    }

    private static ReviewResponse toResponse(Review review, boolean mine) {
        return new ReviewResponse(review.getId(), review.getMovieId(), review.getUsername(),
                review.getRating(), review.getComment(), review.getCreatedAt(), mine);
    }
}
