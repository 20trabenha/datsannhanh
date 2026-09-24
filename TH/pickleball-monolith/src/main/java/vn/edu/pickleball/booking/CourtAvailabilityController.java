package vn.edu.pickleball.booking;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/courts")
public class CourtAvailabilityController {
    private final CourtAvailabilityService service;
    public CourtAvailabilityController(CourtAvailabilityService service) { this.service = service; }

    @GetMapping("/{courtId}/availability")
    public CourtAvailabilityService.AvailabilityResponse availability(@PathVariable Long courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.forDay(courtId, date);
    }
}
