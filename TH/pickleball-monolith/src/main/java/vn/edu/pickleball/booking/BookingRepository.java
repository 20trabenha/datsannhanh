package vn.edu.pickleball.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {
    @Query("select count(b) > 0 from Booking b where b.court.id = :courtId " +
            "and b.startAt < :endAt and b.endAt > :startAt " +
            "and (b.status = 'CONFIRMED' or (b.status = 'PENDING_PAYMENT' " +
            "and (b.paymentDeadline > :now and b.transferSubmittedAt is null or " +
            "b.transferSubmittedAt is not null and (b.transferReviewDeadline is null or b.transferReviewDeadline > :now))))")
    boolean hasActiveOverlap(@Param("courtId") Long courtId, @Param("startAt") LocalDateTime startAt,
                             @Param("endAt") LocalDateTime endAt, @Param("now") LocalDateTime now);
    @Query("select count(b) > 0 from Booking b where b.id <> :bookingId and b.court.id = :courtId " +
            "and b.startAt < :endAt and b.endAt > :startAt " +
            "and (b.status = 'CONFIRMED' or (b.status = 'PENDING_PAYMENT' " +
            "and (b.paymentDeadline > :now and b.transferSubmittedAt is null or " +
            "b.transferSubmittedAt is not null and (b.transferReviewDeadline is null or b.transferReviewDeadline > :now))))")
    boolean hasActiveOverlapExcept(@Param("bookingId") Long bookingId, @Param("courtId") Long courtId,
                                   @Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt,
                                   @Param("now") LocalDateTime now);
    @Query("select b from Booking b where b.court.id = :courtId " +
            "and b.startAt < :endAt and b.endAt > :startAt " +
            "and (b.status = 'CONFIRMED' or (b.status = 'PENDING_PAYMENT' " +
            "and (b.paymentDeadline > :now and b.transferSubmittedAt is null or " +
            "b.transferSubmittedAt is not null and (b.transferReviewDeadline is null or b.transferReviewDeadline > :now)))) " +
            "order by b.startAt")
    List<Booking> findActiveForDay(@Param("courtId") Long courtId, @Param("startAt") LocalDateTime startAt,
                                   @Param("endAt") LocalDateTime endAt, @Param("now") LocalDateTime now);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.id = :id")
    Optional<Booking> findLockedById(@Param("id") Long id);
    List<Booking> findByCustomerIdOrderByStartAtDesc(Long customerId);
    List<Booking> findAllByOrderByStartAtDesc();
    long countByTransferSubmittedAtIsNotNullAndStatusIn(List<BookingStatus> statuses);
    @Query("select b from Booking b where b.status = 'PENDING_PAYMENT' and " +
            "(b.transferSubmittedAt is null and b.paymentDeadline <= :now or " +
            "b.transferSubmittedAt is not null and b.transferReviewDeadline <= :now)")
    List<Booking> findPendingToExpire(@Param("now") LocalDateTime now);
    @Query("select b from Booking b where b.status = 'CONFIRMED' and b.transferSubmittedAt is not null " +
            "and b.transferReviewDeadline <= :now")
    List<Booking> findFullPaymentNoticesToExpire(@Param("now") LocalDateTime now);
    @Query("select count(b) > 0 from Booking b where lower(b.depositBankReference) = lower(:reference) " +
            "or lower(b.fullBankReference) = lower(:reference) or lower(b.refundBankReference) = lower(:reference)")
    boolean bankReferenceExists(@Param("reference") String reference);
}
