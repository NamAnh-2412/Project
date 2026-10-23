package btl.vexemphim.booking.controller;

import btl.vexemphim.booking.dto.ReviewRequest;
import btl.vexemphim.booking.dto.ReviewResponse;
import btl.vexemphim.booking.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies/{movieId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public List<ReviewResponse> list(@PathVariable Long movieId) {
        return reviewService.list(movieId);
    }

    @PostMapping
    public ReviewResponse save(@PathVariable Long movieId, @Valid @RequestBody ReviewRequest req) {
        return reviewService.save(movieId, req);
    }
}
