package btl.vexemphim.catalog.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "movies")
@EntityListeners(AuditingEntityListener.class)
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(length = 16)
    private String rated;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "poster_url", length = 512)
    private String posterUrl;

    /** Link YouTube, không lưu file trailer. */
    @Column(name = "trailer_url", length = 512)
    private String trailerUrl;

    @Column(length = 255)
    private String director;

    /** Diễn viên, lưu chuỗi (BTL không tách bảng cast). */
    @Column(name = "cast_names", length = 512)
    private String castNames;

    @Column(name = "release_date")
    private java.time.LocalDate releaseDate;

    /** SHOWING = đang chiếu, COMING = sắp chiếu, HIDDEN = ngừng chiếu (xóa mềm). */
    @Column(length = 16)
    private String status = "SHOWING";

    @Column(name = "rating_average", precision = 3, scale = 1)
    private java.math.BigDecimal ratingAverage = java.math.BigDecimal.ZERO;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @ManyToOne
    @JoinColumn(name = "genre_id")
    @JsonBackReference
    private Genre genre;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getRated() { return rated; }
    public void setRated(String rated) { this.rated = rated; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public String getTrailerUrl() { return trailerUrl; }
    public void setTrailerUrl(String trailerUrl) { this.trailerUrl = trailerUrl; }
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    public String getCastNames() { return castNames; }
    public void setCastNames(String castNames) { this.castNames = castNames; }
    public java.time.LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(java.time.LocalDate releaseDate) { this.releaseDate = releaseDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Genre getGenre() { return genre; }
    public void setGenre(Genre genre) { this.genre = genre; }
    public java.math.BigDecimal getRatingAverage() {
        return ratingAverage == null ? java.math.BigDecimal.ZERO : ratingAverage;
    }
    public void setRatingAverage(java.math.BigDecimal ratingAverage) { this.ratingAverage = ratingAverage; }
    public int getRatingCount() { return ratingCount == null ? 0 : ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }
}
