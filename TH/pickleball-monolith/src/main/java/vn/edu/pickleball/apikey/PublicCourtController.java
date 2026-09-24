package vn.edu.pickleball.apikey;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import vn.edu.pickleball.court.CourtService;
import vn.edu.pickleball.court.CourtDtos.CourtResponse;

@RestController
@RequestMapping("/api/public/courts")
public class PublicCourtController {
    private final ApiKeyService keys;
    private final CourtService courts;
    public PublicCourtController(ApiKeyService keys, CourtService courts) { this.keys = keys; this.courts = courts; }
    @GetMapping public Page<CourtResponse> list(@RequestHeader(value = "X-API-KEY", required = false) String key,
            @RequestParam(required = false) String keyword, @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {
        if (!keys.canReadCourts(key)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "API Key không hợp lệ.");
        return courts.list(keyword, categoryId, page, size, sort);
    }
}
