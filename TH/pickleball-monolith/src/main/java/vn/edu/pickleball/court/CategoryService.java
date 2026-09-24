package vn.edu.pickleball.court;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.pickleball.court.CourtDtos.*;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CategoryService {
    private final CategoryRepository categories;
    private final CourtRepository courts;
    public CategoryService(CategoryRepository categories, CourtRepository courts) {
        this.categories = categories; this.courts = courts;
    }
    public List<CategoryResponse> list() {
        return categories.findAll().stream().map(this::response).toList();
    }
    public CategoryResponse get(Long id) { return response(find(id)); }
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = validName(request);
        if (categories.existsByNameIgnoreCase(name)) throw new IllegalArgumentException("Danh mục đã tồn tại.");
        return response(categories.save(new Category(name)));
    }
    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = find(id);
        String name = validName(request);
        if (categories.existsByNameIgnoreCaseAndIdNot(name, id)) throw new IllegalArgumentException("Danh mục đã tồn tại.");
        category.setName(name);
        return response(category);
    }
    @Transactional
    public void delete(Long id) {
        Category category = find(id);
        if (courts.existsByCategoryId(id)) throw new IllegalStateException("Danh mục đang có sân, không thể xóa.");
        categories.delete(category);
    }
    Category find(Long id) {
        return categories.findById(id).orElseThrow(() -> new NoSuchElementException("Không tìm thấy danh mục."));
    }
    private String validName(CategoryRequest request) {
        if (request == null || request.name() == null || request.name().isBlank())
            throw new IllegalArgumentException("Tên danh mục không được để trống.");
        return request.name().trim();
    }
    private CategoryResponse response(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
