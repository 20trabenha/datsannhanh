package vn.edu.pickleball.booking;

import java.time.LocalDateTime;

public final class BookingDtos {
    private BookingDtos() {}
    public record BookingRequest(Long courtId, LocalDateTime startAt, LocalDateTime endAt,
                                 String customerName, String customerPhone, String customerNote) {
        public BookingRequest(Long courtId, LocalDateTime startAt, LocalDateTime endAt) {
            this(courtId, startAt, endAt, null, null, null);
        }
    }
    public record TransferNoticeRequest(TransferType type) {}
    public enum AdminPaymentAction { CONFIRM_DEPOSIT, CONFIRM_FULL }
    public enum AdminDecisionAction { REJECT, CANCEL }
    public enum AdminRefundAction { MARK_REQUIRED, NO_PAYMENT_FOUND, CONFIRM_REFUND }
    public record AdminPaymentRequest(AdminPaymentAction action, String transactionReference) {}
    public record AdminDecisionRequest(AdminDecisionAction action, String reason) {}
    public record AdminRefundRequest(AdminRefundAction action, Long amount, String transactionReference) {}
    public record QuoteResponse(Long courtId, long durationMinutes, long totalAmount,
                                long depositAmount, long remainingAmount) {}
    public record BookingResponse(Long id, Long courtId, String courtName, String customerName, String customerPhone,
                                  String customerNote,
                                  LocalDateTime startAt, LocalDateTime endAt, LocalDateTime bookedAt,
                                  BookingStatus status, PaymentStatus paymentStatus, long totalAmount,
                                  long depositAmount, long remainingAmount, LocalDateTime paymentDeadline,
                                  LocalDateTime depositPaidAt, LocalDateTime fullPaidAt,
                                  LocalDateTime transferSubmittedAt, TransferType transferType,
                                  String orderCode, String decisionReason, LocalDateTime decisionAt,
                                  LocalDateTime transferReviewDeadline, String transferReviewNote,
                                  String depositBankReference, String fullBankReference,
                                  String depositConfirmedBy, String fullConfirmedBy, String decisionBy,
                                  RefundStatus refundStatus, Long refundAmount, LocalDateTime refundedAt,
                                  String refundBankReference, String refundConfirmedBy) {}
}
