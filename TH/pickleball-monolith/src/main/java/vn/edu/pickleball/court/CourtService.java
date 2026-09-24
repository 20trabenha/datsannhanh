package vn.edu.pickleball.court;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.pickleball.court.CourtDtos.*;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class CourtService {
    private static final Set<String> SORT_FIELDS = Set.of("name", "pricePerHour", "id");
    private final CourtRepository courts;
    private final CategoryService categories;
    private final FileStorageService files;
    public CourtService(CourtRepository courts, CategoryService categories, FileStorageService files) {
        this.courts = courts; this.categories = categories; this.files = files;
    }
    @Transactional(readOnly = true)
    public Page<CourtResponse> list(String keyword, Long categoryId, int page, int size, String sort) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Tham số phân trang không hợp lệ.");
        String[] parts = sort == null ? new String[]{"id", "asc"} : sort.split(",", -1);
        if (parts.length != 2 || !SORT_FIELDS.contains(parts[0]) || !(parts[1].equals("asc") || parts[1].equals("desc")))
            throw new IllegalArgumentException("Tham số sắp xếp không hợp lệ.");
        Sort order = Sort.by(parts[1].equals("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, parts[0]);
        Pageable pageable = PageRequest.of(page, size, order);
        String name = keyword == null ? "" : keyword.trim();
        Page<Court> result = categoryId == null
                ? (name.isEmpty() ? courts.findAll(pageable) : courts.findByNameContainingIgnoreCase(name, pageable))
                : (name.isEmpty() ? courts.findByCategoryId(categoryId, pageable)
                : courts.findByCategoryIdAndNameContainingIgnoreCase(categoryId, name, pageable));
        return result.map(this::response);
    }
    @Transactional(readOnly = true)
    public CourtResponse get(Long id) { return response(find(id)); }
    @Transactional
    public CourtResponse create(CourtRequest request) {
        validate(request);
        String name = request.name().trim();
        if (courts.existsByNameIgnoreCase(name)) throw new IllegalArgumentException("Tên sân đã tồn tại.");
        Court court = new Court(name, request.pricePerHour(), request.description(), categories.find(request.categoryId()));
        court.setDetails(clean(request.address()), clean(request.amenities()), request.openingTime(), request.closingTime());
        return response(courts.save(court));
    }
    @Transactional
    public CourtResponse update(Long id, CourtRequest request) {
        validate(request);
        Court court = find(id);
        String name = request.name().trim();
        if (courts.existsByNameIgnoreCaseAndIdNot(name, id)) throw new IllegalArgumentException("Tên sân đã tồn tại.");
        court.setName(name); court.setPricePerHour(request.pricePerHour());
        court.setDescription(request.description()); court.setCategory(categories.find(request.categoryId()));
        court.setDetails(clean(request.address()), clean(request.amenities()), request.openingTime(), request.closingTime());
        return response(court);
    }
    @Transactional
    public void delete(Long id) { courts.delete(find(id)); }
    @Transactional
    public CourtResponse uploadImage(Long id, MultipartFile file) {
        Court court = find(id);
        court.setImageUrl(files.save(file));
        return response(court);
    }
    Court find(Long id) { return courts.findById(id).orElseThrow(() -> new NoSuchElementException("Không tìm thấy sân.")); }
    private void validate(CourtRequest request) {
        if (request == null || request.name() == null || request.name().isBlank())
            throw new IllegalArgumentException("Tên sân không được để trống.");
        if (request.pricePerHour() == null || request.pricePerHour() < 1)
            throw new IllegalArgumentException("Giá thuê phải lớn hơn 0.");
        if (request.categoryId() == null) throw new IllegalArgumentException("Vui lòng chọn danh mục sân.");
        if (request.address() != null && request.address().length() > 200)
            throw new IllegalArgumentException("Địa chỉ sân không được quá 200 ký tự.");
        if (request.amenities() != null && request.amenities().length() > 500)
            throw new IllegalArgumentException("Tiện ích không được quá 500 ký tự.");
        if ((request.openingTime() == null) != (request.closingTime() == null) ||
                (request.openingTime() != null && !request.openingTime().isBefore(request.closingTime())))
            throw new IllegalArgumentException("Giờ mở cửa phải trước giờ đóng cửa và cần nhập đủ cả hai.");
    }
    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private CourtResponse response(Court court) {
        Category category = court.getCategory();
        return new CourtResponse(court.getId(), court.getName(), court.getPricePerHour(), court.getDescription(),
                court.getImageUrl(), category.getId(), category.getName(), court.getAddress(),
                court.getAmenities(), court.getOpeningTime(), court.getClosingTime());
    }
}
