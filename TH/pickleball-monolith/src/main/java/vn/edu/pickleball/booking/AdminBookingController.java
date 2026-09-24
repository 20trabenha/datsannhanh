package vn.edu.pickleball.booking;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import vn.edu.pickleball.auth.User;
import vn.edu.pickleball.booking.BookingDtos.BookingResponse;
import vn.edu.pickleball.booking.BookingDtos.AdminDecisionRequest;
import vn.edu.pickleball.booking.BookingDtos.AdminPaymentRequest;
import vn.edu.pickleball.booking.BookingDtos.AdminRefundRequest;
import java.util.List;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/admin/bookings")
public class AdminBookingController {
    private final BookingService service;
    public AdminBookingController(BookingService service) { this.service = service; }
    @GetMapping public Page<BookingResponse> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) Long courtId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.all(page, size, keyword, status, courtId, date);
    }
    @GetMapping("/review-count") public long reviewCount() { return service.reviewCount(); }
    @PostMapping("/{id}/payment") public BookingResponse confirmPayment(@AuthenticationPrincipal User admin,
            @PathVariable Long id, @RequestBody AdminPaymentRequest request) {
        return service.confirmPayment(admin, id, request);
    }
    @PostMapping("/{id}/decision") public BookingResponse decide(@AuthenticationPrincipal User admin,
            @PathVariable Long id, @RequestBody AdminDecisionRequest request) {
        return service.decide(admin, id, request);
    }
    @PostMapping("/{id}/refund") public BookingResponse refund(@AuthenticationPrincipal User admin,
            @PathVariable Long id, @RequestBody AdminRefundRequest request) {
        return service.refund(admin, id, request);
    }
}
