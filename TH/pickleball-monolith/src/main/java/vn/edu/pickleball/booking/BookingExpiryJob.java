package vn.edu.pickleball.booking;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Component
public class BookingExpiryJob {
    private final BookingRepository bookings;
    public BookingExpiryJob(BookingRepository bookings) { this.bookings = bookings; }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expireOverdueBookings() {
        LocalDateTime now = LocalDateTime.now();
        bookings.findPendingToExpire(now).forEach(Booking::expirePending);
        bookings.findFullPaymentNoticesToExpire(now).forEach(Booking::expireFullPaymentNotice);
    }
}
