package vn.edu.pickleball.booking;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import vn.edu.pickleball.auth.User;
import vn.edu.pickleball.court.Court;
import vn.edu.pickleball.court.CourtRepository;
import vn.edu.pickleball.court.CourtBlockRepository;
import vn.edu.pickleball.booking.BookingDtos.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Locale;
import java.util.ArrayList;
import jakarta.persistence.criteria.Predicate;

@Service
public class BookingService {
    private final BookingRepository bookings;
    private final CourtRepository courts;
    private final CourtBlockRepository blocks;
    public BookingService(BookingRepository bookings, CourtRepository courts, CourtBlockRepository blocks) {
        this.bookings = bookings; this.courts = courts; this.blocks = blocks;
    }
    @Transactional
    public BookingResponse book(User customer, BookingRequest request) {
        validate(request);
        validateContact(request);
        Court court = courts.findLockedById(request.courtId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sân."));
        validateCourtHours(court, request);
        validateNotBlocked(court, request);
        if (bookings.hasActiveOverlap(court.getId(), request.startAt(), request.endAt(), LocalDateTime.now()))
            throw new IllegalStateException("Sân đã được đặt trong khoảng thời gian này.");
        QuoteResponse quote = quoteFor(court, request);
        return response(bookings.save(new Booking(customer, court, request.startAt(), request.endAt(),
                quote.totalAmount(), quote.depositAmount(), request.customerName().trim(),
                request.customerPhone().trim(), trimToNull(request.customerNote()))));
    }
    @Transactional(readOnly = true)
    public QuoteResponse quote(BookingRequest request) {
        validate(request);
        Court court = courts.findById(request.courtId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sân."));
        validateCourtHours(court, request);
        validateNotBlocked(court, request);
        return quoteFor(court, request);
    }
    @Transactional(readOnly = true)
    public BookingResponse get(User customer, Long id) { return response(owned(customer, id)); }
    @Transactional(readOnly = true)
    public List<BookingResponse> mine(User customer) {
        return bookings.findByCustomerIdOrderByStartAtDesc(customer.getId()).stream().map(this::response).toList();
    }
    @Transactional(readOnly = true)
    public Page<BookingResponse> all(int page, int size, String keyword, BookingStatus status,
                                     Long courtId, LocalDate date) {
        if (page < 0 || size < 1 || size > 50)
            throw new IllegalArgumentException("Tham số phân trang không hợp lệ.");
        String search = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        Specification<Booking> criteria = (root, query, cb) -> {
            List<Predicate> parts = new ArrayList<>();
            if (status != null) parts.add(cb.equal(root.get("status"), status));
            if (courtId != null) parts.add(cb.equal(root.get("court").get("id"), courtId));
            if (date != null) {
                parts.add(cb.greaterThanOrEqualTo(root.get("startAt"), date.atStartOfDay()));
                parts.add(cb.lessThan(root.get("startAt"), date.plusDays(1).atStartOfDay()));
            }
            if (!search.isEmpty()) {
                String pattern = "%" + search + "%";
                List<Predicate> matches = new ArrayList<>();
                matches.add(cb.like(cb.lower(root.get("contactName")), pattern));
                matches.add(cb.like(cb.lower(root.get("contactPhone")), pattern));
                matches.add(cb.like(cb.lower(root.get("customer").get("username")), pattern));
                matches.add(cb.like(cb.lower(root.get("court").get("name")), pattern));
                if (search.matches("pb[0-9]+")) {
                    try { matches.add(cb.equal(root.get("id"), Long.parseLong(search.substring(2)))); }
                    catch (NumberFormatException ignored) { /* Search by the other fields. */ }
                }
                parts.add(cb.or(matches.toArray(Predicate[]::new)));
            }
            return cb.and(parts.toArray(Predicate[]::new));
        };
        return bookings.findAll(criteria, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startAt", "id")))
                .map(this::response);
    }
    @Transactional(readOnly = true)
    public long reviewCount() {
        return bookings.countByTransferSubmittedAtIsNotNullAndStatusIn(
                List.of(BookingStatus.PENDING_PAYMENT, BookingStatus.CONFIRMED));
    }
    @Transactional
    public BookingResponse submitTransferNotice(User customer, Long id, TransferType type) {
        Booking booking = bookings.findLockedById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy lượt đặt sân."));
        if (!booking.getCustomer().getId().equals(customer.getId()))
            throw new NoSuchElementException("Không tìm thấy lượt đặt sân.");
        if (type == null) throw new IllegalArgumentException("Vui lòng chọn cọc hoặc thanh toán đủ.");
        if (booking.getDepositAmount() == null ||
                (effectiveStatus(booking) != BookingStatus.PENDING_PAYMENT && booking.getStatus() != BookingStatus.CONFIRMED))
            throw new IllegalStateException("Lượt đặt sân không thể báo chuyển khoản.");
        if (booking.getFullPaidAt() != null ||
                (type == TransferType.DEPOSIT && booking.getDepositPaidAt() != null))
            throw new IllegalStateException("Khoản tiền này đã được ghi nhận.");
        if (booking.getTransferSubmittedAt() != null)
            throw new IllegalStateException("Đã báo chuyển khoản, vui lòng chờ quản trị viên kiểm tra.");
        booking.submitTransfer(type);
        return response(booking);
    }
    @Transactional
    public BookingResponse confirmPayment(User admin, Long id, AdminPaymentRequest request) {
        if (request == null || request.action() == null)
            throw new IllegalArgumentException("Vui lòng chọn khoản tiền cần xác nhận.");
        String reference = validBankReference(request.transactionReference());
        Booking booking = bookings.findLockedById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy lượt đặt sân."));
        if (booking.getDepositAmount() == null || booking.getStatus() == BookingStatus.CANCELLED ||
                booking.getStatus() == BookingStatus.REJECTED)
            throw new IllegalStateException("Lượt đặt sân không thể xác nhận thanh toán.");
        if (effectiveStatus(booking) == BookingStatus.EXPIRED) {
            courts.findLockedById(booking.getCourt().getId())
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sân."));
            if (blocks.hasOverlap(booking.getCourt().getId(), booking.getStartAt(), booking.getEndAt()))
                throw new IllegalStateException("Sân đang khóa trong thời gian này.");
            if (bookings.hasActiveOverlapExcept(booking.getId(), booking.getCourt().getId(),
                    booking.getStartAt(), booking.getEndAt(), LocalDateTime.now()))
                throw new IllegalStateException("Sân đã được người khác đặt trong thời gian này.");
        }
        if (bookings.bankReferenceExists(reference))
            throw new IllegalArgumentException("Mã giao dịch này đã được ghi nhận cho một đơn khác.");
        switch (request.action()) {
            case CONFIRM_DEPOSIT -> {
                if (booking.getDepositPaidAt() != null)
                    throw new IllegalStateException("Tiền cọc đã được xác nhận.");
                booking.confirmDeposit(reference, admin.getUsername());
            }
            case CONFIRM_FULL -> {
                if (booking.getFullPaidAt() != null)
                    throw new IllegalStateException("Đã xác nhận thanh toán đủ.");
                booking.confirmFull(reference, admin.getUsername());
            }
        }
        return response(booking);
    }
    @Transactional
    public BookingResponse decide(User admin, Long id, AdminDecisionRequest request) {
        if (request == null || request.action() == null || request.reason() == null || request.reason().isBlank())
            throw new IllegalArgumentException("Vui lòng nhập lý do từ chối hoặc hủy đơn.");
        String reason = request.reason().trim();
        if (reason.length() > 500)
            throw new IllegalArgumentException("Lý do không được quá 500 ký tự.");
        Booking booking = bookings.findLockedById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy lượt đặt sân."));
        if (effectiveStatus(booking) == BookingStatus.EXPIRED || booking.getStatus() == BookingStatus.CANCELLED ||
                booking.getStatus() == BookingStatus.REJECTED)
            throw new IllegalStateException("Lượt đặt sân này đã kết thúc.");
        if (request.action() == AdminDecisionAction.REJECT) {
            if (booking.getStatus() != BookingStatus.PENDING_PAYMENT || booking.getDepositPaidAt() != null)
                throw new IllegalStateException("Chỉ có thể từ chối đơn đang chờ cọc.");
            booking.reject(reason, admin.getUsername());
        } else booking.cancelByAdmin(reason, admin.getUsername());
        return response(booking);
    }
    @Transactional
    public BookingResponse refund(User admin, Long id, AdminRefundRequest request) {
        if (request == null || request.action() == null)
            throw new IllegalArgumentException("Vui lòng chọn cách xử lý hoàn tiền.");
        Booking booking = bookings.findLockedById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy lượt đặt sân."));
        if (booking.getStatus() != BookingStatus.CANCELLED && booking.getStatus() != BookingStatus.REJECTED &&
                effectiveStatus(booking) != BookingStatus.EXPIRED)
            throw new IllegalStateException("Chỉ có thể xử lý hoàn tiền cho đơn đã kết thúc.");
        if (booking.getStatus() == BookingStatus.PENDING_PAYMENT && effectiveStatus(booking) == BookingStatus.EXPIRED)
            booking.expirePending();
        switch (request.action()) {
            case MARK_REQUIRED -> {
                if (booking.getRefundStatus() != RefundStatus.REVIEW_REQUIRED && booking.getRefundStatus() != RefundStatus.NONE)
                    throw new IllegalStateException("Đơn này đã được đưa vào quy trình hoàn tiền.");
                if (request.amount() == null || request.amount() < 1 ||
                        booking.getTotalAmount() == null || request.amount() > booking.getTotalAmount())
                    throw new IllegalArgumentException("Số tiền hoàn phải lớn hơn 0 và không vượt tổng tiền sân.");
                booking.requireRefund(request.amount());
            }
            case NO_PAYMENT_FOUND -> {
                if (booking.getRefundStatus() != RefundStatus.REVIEW_REQUIRED)
                    throw new IllegalStateException("Đơn này không chờ đối chiếu chuyển khoản.");
                booking.markNoPaymentFound();
            }
            case CONFIRM_REFUND -> {
                if (booking.getRefundStatus() != RefundStatus.PENDING)
                    throw new IllegalStateException("Đơn này không chờ hoàn tiền.");
                String reference = validBankReference(request.transactionReference());
                if (bookings.bankReferenceExists(reference))
                    throw new IllegalArgumentException("Mã giao dịch này đã được ghi nhận cho một đơn khác.");
                booking.confirmRefund(reference, admin.getUsername());
            }
        }
        return response(booking);
    }
    @Transactional
    public void cancel(User customer, Long id) {
        Booking booking = owned(customer, id);
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.REJECTED ||
                effectiveStatus(booking) == BookingStatus.EXPIRED)
            throw new IllegalStateException("Lượt đặt sân không thể hủy.");
        if (booking.getTransferSubmittedAt() != null || booking.getDepositPaidAt() != null)
            throw new IllegalStateException("Vui lòng liên hệ quản trị viên để hủy lượt đặt đã báo chuyển khoản hoặc đã cọc.");
        booking.cancel();
    }
    private Booking owned(User customer, Long id) {
        Booking booking = bookings.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy lượt đặt sân."));
        if (!booking.getCustomer().getId().equals(customer.getId()))
            throw new NoSuchElementException("Không tìm thấy lượt đặt sân.");
        return booking;
    }
    private void validate(BookingRequest request) {
        if (request == null || request.courtId() == null || request.startAt() == null || request.endAt() == null)
            throw new IllegalArgumentException("Vui lòng chọn sân, giờ bắt đầu và giờ kết thúc.");
        if (!request.startAt().isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Giờ đặt sân phải ở trong tương lai.");
        if (!request.endAt().isAfter(request.startAt()))
            throw new IllegalArgumentException("Giờ kết thúc phải sau giờ bắt đầu.");
        if (request.startAt().getSecond() != 0 || request.startAt().getNano() != 0 ||
                request.endAt().getSecond() != 0 || request.endAt().getNano() != 0)
            throw new IllegalArgumentException("Vui lòng chọn thời gian theo phút.");
    }
    private void validateContact(BookingRequest request) {
        if (request.customerName() == null || request.customerName().isBlank())
            throw new IllegalArgumentException("Vui lòng nhập họ và tên người đặt sân.");
        if (request.customerName().trim().length() > 100)
            throw new IllegalArgumentException("Họ và tên không được quá 100 ký tự.");
        if (request.customerPhone() == null || !request.customerPhone().trim().matches("[0-9]{10}"))
            throw new IllegalArgumentException("Số điện thoại phải gồm đúng 10 chữ số.");
        if (request.customerNote() != null && request.customerNote().trim().length() > 500)
            throw new IllegalArgumentException("Ghi chú không được quá 500 ký tự.");
    }
    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
    private String validBankReference(String value) {
        if (value == null || value.trim().length() < 4 || value.trim().length() > 100)
            throw new IllegalArgumentException("Vui lòng nhập mã giao dịch ngân hàng từ 4 đến 100 ký tự.");
        return value.trim();
    }
    private QuoteResponse quoteFor(Court court, BookingRequest request) {
        long minutes = Duration.between(request.startAt(), request.endAt()).toMinutes();
        long total = BigDecimal.valueOf(court.getPricePerHour()).multiply(BigDecimal.valueOf(minutes))
                .divide(BigDecimal.valueOf(60), 0, RoundingMode.UP).longValueExact();
        long deposit = BigDecimal.valueOf(total).divide(BigDecimal.TEN, 0, RoundingMode.UP).longValueExact();
        return new QuoteResponse(court.getId(), minutes, total, deposit, total - deposit);
    }
    private void validateCourtHours(Court court, BookingRequest request) {
        if (court.getOpeningTime() == null || court.getClosingTime() == null) return;
        if (!request.startAt().toLocalDate().equals(request.endAt().toLocalDate()) ||
                request.startAt().toLocalTime().isBefore(court.getOpeningTime()) ||
                request.endAt().toLocalTime().isAfter(court.getClosingTime()))
            throw new IllegalArgumentException("Vui lòng chọn giờ trong khoảng hoạt động của sân.");
    }
    private void validateNotBlocked(Court court, BookingRequest request) {
        if (blocks.hasOverlap(court.getId(), request.startAt(), request.endAt()))
            throw new IllegalStateException("Sân tạm ngừng hoạt động trong khung giờ này.");
    }
    private BookingStatus effectiveStatus(Booking booking) {
        if (booking.getStatus() == BookingStatus.PENDING_PAYMENT &&
                (booking.getTransferSubmittedAt() == null && booking.getPaymentDeadline() != null &&
                        !booking.getPaymentDeadline().isAfter(LocalDateTime.now()) ||
                 booking.getTransferSubmittedAt() != null && booking.getTransferReviewDeadline() != null &&
                        !booking.getTransferReviewDeadline().isAfter(LocalDateTime.now()))) return BookingStatus.EXPIRED;
        return booking.getStatus();
    }
    private BookingResponse response(Booking booking) {
        long total = booking.getTotalAmount() == null ? 0 : booking.getTotalAmount();
        long deposit = booking.getDepositAmount() == null ? 0 : booking.getDepositAmount();
        PaymentStatus payment = booking.getDepositAmount() == null ? PaymentStatus.NOT_APPLICABLE :
                booking.getFullPaidAt() != null ? PaymentStatus.FULLY_PAID :
                booking.getDepositPaidAt() != null ? PaymentStatus.DEPOSIT_PAID : PaymentStatus.PENDING;
        return new BookingResponse(booking.getId(), booking.getCourt().getId(), booking.getCourt().getName(),
                booking.getContactName() != null ? booking.getContactName() : booking.getCustomer().getUsername(),
                booking.getContactPhone() != null ? booking.getContactPhone() : booking.getCustomer().getPhoneNumber(),
                booking.getCustomerNote(),
                booking.getStartAt(), booking.getEndAt(), booking.getBookedAt(), effectiveStatus(booking), payment,
                total, deposit, total - deposit, booking.getPaymentDeadline(),
                booking.getDepositPaidAt(), booking.getFullPaidAt(), booking.getTransferSubmittedAt(),
                booking.getTransferType(), String.format(Locale.ROOT, "PB%06d", booking.getId()),
                booking.getDecisionReason(), booking.getDecisionAt(), booking.getTransferReviewDeadline(),
                booking.getTransferReviewNote(), booking.getDepositBankReference(), booking.getFullBankReference(),
                booking.getDepositConfirmedBy(), booking.getFullConfirmedBy(), booking.getDecisionBy(),
                booking.getRefundStatus(), booking.getRefundAmount(), booking.getRefundedAt(),
                booking.getRefundBankReference(), booking.getRefundConfirmedBy());
    }
}
