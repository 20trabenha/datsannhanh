package vn.edu.pickleball.booking;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.pickleball.court.Court;
import vn.edu.pickleball.court.CourtRepository;
import vn.edu.pickleball.court.CourtBlock;
import vn.edu.pickleball.court.CourtBlockRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CourtAvailabilityService {
    private final CourtRepository courts;
    private final BookingRepository bookings;
    private final CourtBlockRepository blocks;

    public CourtAvailabilityService(CourtRepository courts, BookingRepository bookings, CourtBlockRepository blocks) {
        this.courts = courts; this.bookings = bookings; this.blocks = blocks;
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse forDay(Long courtId, LocalDate date) {
        if (date == null) throw new IllegalArgumentException("Vui lòng chọn ngày xem lịch sân.");
        Court court = courts.findById(courtId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sân."));
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        List<Booking> active = bookings.findActiveForDay(courtId, dayStart, dayEnd, now);
        List<CourtBlock> blocked = blocks.findForPeriod(courtId, dayStart, dayEnd);
        List<BookedWindow> booked = active.stream()
                .map(item -> new BookedWindow(item.getStartAt(), item.getEndAt())).toList();
        List<ClosedWindow> closed = blocked.stream()
                .map(item -> new ClosedWindow(item.getStartAt(), item.getEndAt(), item.getReason())).toList();
        boolean configured = court.getOpeningTime() != null && court.getClosingTime() != null;
        LocalDateTime cursor = configured ? date.atTime(court.getOpeningTime()) : dayStart;
        LocalDateTime close = configured ? date.atTime(court.getClosingTime()) : dayEnd;
        List<Slot> slots = new ArrayList<>();
        while (cursor.isBefore(close)) {
            LocalDateTime end = cursor.plusMinutes(30);
            if (end.isAfter(close)) end = close;
            LocalDateTime slotStart = cursor;
            LocalDateTime slotEnd = end;
            boolean occupied = active.stream().anyMatch(item ->
                    item.getStartAt().isBefore(slotEnd) && item.getEndAt().isAfter(slotStart));
            boolean unavailable = blocked.stream().anyMatch(item ->
                    item.getStartAt().isBefore(slotEnd) && item.getEndAt().isAfter(slotStart));
            String state = occupied ? "BOOKED" : unavailable ? "CLOSED" : !slotStart.isAfter(now) ? "PAST" : "AVAILABLE";
            slots.add(new Slot(slotStart, slotEnd, state));
            cursor = end;
        }
        return new AvailabilityResponse(courtId, date, configured, booked, closed, slots);
    }

    public record BookedWindow(LocalDateTime startAt, LocalDateTime endAt) {}
    public record ClosedWindow(LocalDateTime startAt, LocalDateTime endAt, String reason) {}
    public record Slot(LocalDateTime startAt, LocalDateTime endAt, String status) {}
    public record AvailabilityResponse(Long courtId, LocalDate date, boolean hoursConfigured,
                                       List<BookedWindow> booked, List<ClosedWindow> closed, List<Slot> slots) {}
}
