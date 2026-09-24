package vn.edu.pickleball.court;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.pickleball.court.CourtDtos.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService service;
    public CategoryController(CategoryService service) { this.service = service; }
    @GetMapping public List<CategoryResponse> list() { return service.list(); }
    @GetMapping("/{id}") public CategoryResponse get(@PathVariable Long id) { return service.get(id); }
    @PostMapping public ResponseEntity<CategoryResponse> create(@RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
    @PutMapping("/{id}") public CategoryResponse update(@PathVariable Long id, @RequestBody CategoryRequest request) {
        return service.update(id, request);
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id); return ResponseEntity.noContent().build();
    }
}
