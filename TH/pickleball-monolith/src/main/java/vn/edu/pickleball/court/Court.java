package vn.edu.pickleball.court;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "courts")
public class Court {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    @Column(nullable = false)
    private Integer pricePerHour;
    @Column(length = 500)
    private String description;
    private String imageUrl;
    @Column(length = 200)
    private String address;
    @Column(length = 500)
    private String amenities;
    private LocalTime openingTime;
    private LocalTime closingTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    protected Court() {}
    public Court(String name, Integer pricePerHour, String description, Category category) {
        this.name = name; this.pricePerHour = pricePerHour; this.description = description; this.category = category;
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getPricePerHour() { return pricePerHour; }
    public void setPricePerHour(Integer pricePerHour) { this.pricePerHour = pricePerHour; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getAddress() { return address; }
    public String getAmenities() { return amenities; }
    public LocalTime getOpeningTime() { return openingTime; }
    public LocalTime getClosingTime() { return closingTime; }
    public void setDetails(String address, String amenities, LocalTime openingTime, LocalTime closingTime) {
        this.address = address; this.amenities = amenities;
        this.openingTime = openingTime; this.closingTime = closingTime;
    }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
}
