package vn.edu.pickleball.court;

import java.time.LocalTime;

public final class CourtDtos {
    private CourtDtos() {}
    public record CategoryRequest(String name) {}
    public record CategoryResponse(Long id, String name) {}
    public record CourtRequest(String name, Integer pricePerHour, String description, Long categoryId,
                               String address, String amenities, LocalTime openingTime, LocalTime closingTime) {}
    public record CourtResponse(Long id, String name, Integer pricePerHour, String description,
                                String imageUrl, Long categoryId, String categoryName,
                                String address, String amenities, LocalTime openingTime, LocalTime closingTime) {}
}
