package vn.edu.pickleball.booking;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import vn.edu.pickleball.auth.User;
import vn.edu.pickleball.booking.BookingDtos.*;
import java.util.List;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService service;
    public BookingController(BookingService service) { this.service = service; }
    @PostMapping public ResponseEntity<BookingResponse> book(@AuthenticationPrincipal User customer,
            @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.book(customer, request));
    }
    @GetMapping("/quote") public QuoteResponse quote(@RequestParam Long courtId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt) {
        return service.quote(new BookingRequest(courtId, startAt, endAt));
    }
    @GetMapping("/my") public List<BookingResponse> mine(@AuthenticationPrincipal User customer) {
        return service.mine(customer);
    }
    @GetMapping("/{id}") public BookingResponse get(@AuthenticationPrincipal User customer, @PathVariable Long id) {
        return service.get(customer, id);
    }
    @PostMapping("/{id}/transfer-notice")
    public BookingResponse submitTransferNotice(@AuthenticationPrincipal User customer, @PathVariable Long id,
            @RequestBody TransferNoticeRequest request) {
        return service.submitTransferNotice(customer, id, request == null ? null : request.type());
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> cancel(@AuthenticationPrincipal User customer, @PathVariable Long id) {
        service.cancel(customer, id); return ResponseEntity.noContent().build();
    }
}
