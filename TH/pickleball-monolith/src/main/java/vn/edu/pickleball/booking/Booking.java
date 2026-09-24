package vn.edu.pickleball.booking;

import jakarta.persistence.*;
import vn.edu.pickleball.auth.User;
import vn.edu.pickleball.court.Court;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User customer;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Court court;
    @Column(length = 100)
    private String contactName;
    @Column(length = 10)
    private String contactPhone;
    @Column(length = 500)
    private String customerNote;
    @Column(nullable = false)
    private LocalDateTime startAt;
    @Column(nullable = false)
    private LocalDateTime endAt;
    @Column(nullable = false)
    private LocalDateTime bookedAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;
    private Long totalAmount;
    private Long depositAmount;
    private LocalDateTime paymentDeadline;
    private LocalDateTime depositPaidAt;
    private LocalDateTime fullPaidAt;
    private LocalDateTime transferSubmittedAt;
    private LocalDateTime transferReviewDeadline;
    @Column(length = 300)
    private String transferReviewNote;
    @Enumerated(EnumType.STRING)
    private TransferType transferType;
    private String depositReceiptFileName;
    private String depositReceiptContentType;
    private LocalDateTime depositReceiptUploadedAt;
    private String fullReceiptFileName;
    private String fullReceiptContentType;
    private LocalDateTime fullReceiptUploadedAt;
    private String paymentReference;
    @Column(length = 100)
    private String depositBankReference;
    @Column(length = 100)
    private String fullBankReference;
    @Column(length = 100)
    private String depositConfirmedBy;
    @Column(length = 100)
    private String fullConfirmedBy;
    @Column(length = 500)
    private String decisionReason;
    private LocalDateTime decisionAt;
    @Column(length = 100)
    private String decisionBy;
    @Enumerated(EnumType.STRING)
    private RefundStatus refundStatus;
    private Long refundAmount;
    private LocalDateTime refundedAt;
    @Column(length = 100)
    private String refundBankReference;
    @Column(length = 100)
    private String refundConfirmedBy;
    protected Booking() {}
    public Booking(User customer, Court court, LocalDateTime startAt, LocalDateTime endAt,
                   long totalAmount, long depositAmount, String contactName, String contactPhone, String customerNote) {
        this.customer = customer; this.court = court; this.startAt = startAt; this.endAt = endAt;
        this.contactName = contactName; this.contactPhone = contactPhone; this.customerNote = customerNote;
        this.bookedAt = LocalDateTime.now(); this.status = BookingStatus.PENDING_PAYMENT;
        this.totalAmount = totalAmount; this.depositAmount = depositAmount;
        this.paymentDeadline = this.bookedAt.plusMinutes(15);
    }
    public Long getId() { return id; }
    public User getCustomer() { return customer; }
    public Court getCourt() { return court; }
    public String getContactName() { return contactName; }
    public String getContactPhone() { return contactPhone; }
    public String getCustomerNote() { return customerNote; }
    public LocalDateTime getStartAt() { return startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public LocalDateTime getBookedAt() { return bookedAt; }
    public BookingStatus getStatus() { return status; }
    public Long getTotalAmount() { return totalAmount; }
    public Long getDepositAmount() { return depositAmount; }
    public LocalDateTime getPaymentDeadline() { return paymentDeadline; }
    public LocalDateTime getDepositPaidAt() { return depositPaidAt; }
    public LocalDateTime getFullPaidAt() { return fullPaidAt; }
    public LocalDateTime getTransferSubmittedAt() { return transferSubmittedAt; }
    public LocalDateTime getTransferReviewDeadline() { return transferReviewDeadline; }
    public String getTransferReviewNote() { return transferReviewNote; }
    public TransferType getTransferType() { return transferType; }
    public String getPaymentReference() { return paymentReference; }
    public String getDepositBankReference() { return depositBankReference; }
    public String getFullBankReference() { return fullBankReference; }
    public String getDepositConfirmedBy() { return depositConfirmedBy; }
    public String getFullConfirmedBy() { return fullConfirmedBy; }
    public String getDecisionReason() { return decisionReason; }
    public LocalDateTime getDecisionAt() { return decisionAt; }
    public String getDecisionBy() { return decisionBy; }
    public RefundStatus getRefundStatus() { return refundStatus == null ? RefundStatus.NONE : refundStatus; }
    public Long getRefundAmount() { return refundAmount; }
    public LocalDateTime getRefundedAt() { return refundedAt; }
    public String getRefundBankReference() { return refundBankReference; }
    public String getRefundConfirmedBy() { return refundConfirmedBy; }
    public void submitTransfer(TransferType type) {
        this.transferType = type;
        this.transferSubmittedAt = LocalDateTime.now();
        this.transferReviewDeadline = transferSubmittedAt.plusHours(2);
        if (startAt.isBefore(transferReviewDeadline)) this.transferReviewDeadline = startAt;
        this.transferReviewNote = null;
    }
    public void setLegacyTransferReviewDeadline(LocalDateTime deadline) { this.transferReviewDeadline = deadline; }
    public void expirePending() {
        this.status = BookingStatus.EXPIRED;
        if (transferSubmittedAt != null) {
            if (getRefundStatus() == RefundStatus.NONE) this.refundStatus = RefundStatus.REVIEW_REQUIRED;
            this.transferReviewNote = "Hết hạn kiểm tra chuyển khoản. Admin cần đối chiếu giao dịch và xử lý tiền nếu đã nhận.";
        }
    }
    public void expireFullPaymentNotice() {
        this.transferType = null; this.transferSubmittedAt = null; this.transferReviewDeadline = null;
        this.transferReviewNote = "Chưa xác nhận được khoản thanh toán còn lại trong thời hạn kiểm tra.";
    }
    private void clearTransferNotice() {
        this.transferType = null; this.transferSubmittedAt = null; this.transferReviewDeadline = null;
        this.transferReviewNote = null;
    }
    public void confirmDeposit(String reference, String adminName) {
        this.depositPaidAt = LocalDateTime.now(); this.paymentReference = reference;
        this.depositBankReference = reference; this.depositConfirmedBy = adminName;
        clearTransferNotice();
        this.refundStatus = RefundStatus.NONE; this.refundAmount = null;
        this.status = BookingStatus.CONFIRMED;
    }
    public void confirmFull(String reference, String adminName) {
        LocalDateTime now = LocalDateTime.now();
        if (this.depositPaidAt == null) {
            this.depositPaidAt = now; this.depositBankReference = reference; this.depositConfirmedBy = adminName;
        }
        this.fullPaidAt = now; this.paymentReference = reference;
        this.fullBankReference = reference; this.fullConfirmedBy = adminName;
        clearTransferNotice();
        this.refundStatus = RefundStatus.NONE; this.refundAmount = null;
        this.status = BookingStatus.CONFIRMED;
    }
    public void expire() { expirePending(); }
    public void cancel() { this.status = BookingStatus.CANCELLED; }
    private void prepareRefund() {
        if (fullPaidAt != null) { refundStatus = RefundStatus.PENDING; refundAmount = totalAmount; }
        else if (depositPaidAt != null) { refundStatus = RefundStatus.PENDING; refundAmount = depositAmount; }
        else if (transferSubmittedAt != null) refundStatus = RefundStatus.REVIEW_REQUIRED;
        else refundStatus = RefundStatus.NONE;
    }
    public void reject(String reason, String adminName) {
        this.status = BookingStatus.REJECTED;
        this.decisionReason = reason;
        this.decisionAt = LocalDateTime.now();
        this.decisionBy = adminName;
        prepareRefund();
    }
    public void cancelByAdmin(String reason, String adminName) {
        this.status = BookingStatus.CANCELLED;
        this.decisionReason = reason;
        this.decisionAt = LocalDateTime.now();
        this.decisionBy = adminName;
        prepareRefund();
    }
    public void requireRefund(long amount) { this.refundAmount = amount; this.refundStatus = RefundStatus.PENDING; }
    public void markNoPaymentFound() { this.refundAmount = null; this.refundStatus = RefundStatus.NONE; }
    public void confirmRefund(String reference, String adminName) {
        this.refundStatus = RefundStatus.REFUNDED; this.refundedAt = LocalDateTime.now();
        this.refundBankReference = reference; this.refundConfirmedBy = adminName;
    }
}
