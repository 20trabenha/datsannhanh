package vn.edu.pickleball.court;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.pickleball.court.CourtBlockService.BlockRequest;
import vn.edu.pickleball.court.CourtBlockService.BlockResponse;
import java.util.List;

@RestController
@RequestMapping("/api/admin/court-blocks")
public class AdminCourtBlockController {
    private final CourtBlockService service;
    public AdminCourtBlockController(CourtBlockService service) { this.service = service; }
    @GetMapping public List<BlockResponse> list(@RequestParam(required = false) Long courtId) { return service.list(courtId); }
    @PostMapping public ResponseEntity<BlockResponse> create(@RequestBody BlockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id); return ResponseEntity.noContent().build();
    }
}
