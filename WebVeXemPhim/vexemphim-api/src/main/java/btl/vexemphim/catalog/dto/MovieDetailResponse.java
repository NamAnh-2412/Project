package btl.vexemphim.catalog.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovieDetailResponse(
        Long id,
        String title,
        int durationMinutes,
        String rated,
        String description,
        String posterUrl,
        Long genreId,
        String genreName,
        String status,
        String trailerUrl,
        String director,
        String castNames,
        LocalDate releaseDate,
        BigDecimal ratingAverage,
        int ratingCount
) {
}
