package vn.edu.pickleball.apikey;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.pickleball.apikey.ApiKeyDtos.*;
import java.util.List;

@RestController
@RequestMapping("/api/api-keys")
public class ApiKeyController {
    private final ApiKeyService service;
    public ApiKeyController(ApiKeyService service) { this.service = service; }
    @GetMapping public List<ApiKeyResponse> list() { return service.list(); }
    @PostMapping public ResponseEntity<ApiKeyResponse> create(@RequestBody CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> revoke(@PathVariable Long id) {
        service.revoke(id); return ResponseEntity.noContent().build();
    }
}
