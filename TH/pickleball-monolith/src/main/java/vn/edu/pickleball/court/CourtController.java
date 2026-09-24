package vn.edu.pickleball.court;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.pickleball.court.CourtDtos.*;

@RestController
@RequestMapping("/api/courts")
public class CourtController {
    private final CourtService service;
    public CourtController(CourtService service) { this.service = service; }
    @GetMapping public Page<CourtResponse> list(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "id,asc") String sort) {
        return service.list(keyword, categoryId, page, size, sort);
    }
    @GetMapping("/{id}") public CourtResponse get(@PathVariable Long id) { return service.get(id); }
    @PostMapping public ResponseEntity<CourtResponse> create(@RequestBody CourtRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
    @PutMapping("/{id}") public CourtResponse update(@PathVariable Long id, @RequestBody CourtRequest request) {
        return service.update(id, request);
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id); return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/image") public CourtResponse upload(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return service.uploadImage(id, file);
    }
}
