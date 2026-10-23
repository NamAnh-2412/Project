package btl.vexemphim.catalog.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "concession_products")
public class ConcessionProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 128)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    /** FOOD | DRINK | COMBO */
    @Column(nullable = false, length = 16)
    private String type;

    /** ACTIVE | HIDDEN */
    @Column(nullable = false, length = 16)
    private String status = "ACTIVE";

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "badge", length = 64)
    private String badge;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }
}
