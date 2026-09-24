package vn.edu.pickleball.court;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.pickleball.booking.BookingRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CourtBlockService {
    private final CourtBlockRepository blocks;
    private final CourtRepository courts;
    private final BookingRepository bookings;
    public CourtBlockService(CourtBlockRepository blocks, CourtRepository courts, BookingRepository bookings) {
        this.blocks = blocks; this.courts = courts; this.bookings = bookings;
    }
    public record BlockRequest(Long courtId, LocalDateTime startAt, LocalDateTime endAt, String reason) {}
    public record BlockResponse(Long id, Long courtId, String courtName, LocalDateTime startAt,
                                LocalDateTime endAt, String reason) {}
    @Transactional(readOnly = true)
    public List<BlockResponse> list(Long courtId) {
        List<CourtBlock> result = courtId == null ? blocks.findAllByOrderByStartAtDesc() :
                blocks.findByCourtIdOrderByStartAtDesc(courtId);
        return result.stream().map(this::response).toList();
    }
    @Transactional
    public BlockResponse create(BlockRequest request) {
        if (request == null || request.courtId() == null || request.startAt() == null || request.endAt() == null ||
                request.reason() == null || request.reason().isBlank())
            throw new IllegalArgumentException("Vui lòng chọn sân, thời gian và nhập lý do khóa sân.");
        if (!request.startAt().isAfter(LocalDateTime.now()) || !request.endAt().isAfter(request.startAt()))
            throw new IllegalArgumentException("Khoảng khóa sân phải ở tương lai và giờ kết thúc sau giờ bắt đầu.");
        if (request.reason().trim().length() > 300)
            throw new IllegalArgumentException("Lý do không được quá 300 ký tự.");
        Court court = courts.findLockedById(request.courtId())
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy sân."));
        if (blocks.hasOverlap(court.getId(), request.startAt(), request.endAt()))
            throw new IllegalStateException("Khung giờ này đã được khóa.");
        if (bookings.hasActiveOverlap(court.getId(), request.startAt(), request.endAt(), LocalDateTime.now()))
            throw new IllegalStateException("Khung giờ đã có đơn đặt. Vui lòng xử lý đơn trước khi khóa sân.");
        return response(blocks.save(new CourtBlock(court, request.startAt(), request.endAt(), request.reason().trim())));
    }
    @Transactional
    public void delete(Long id) {
        CourtBlock block = blocks.findById(id).orElseThrow(() -> new NoSuchElementException("Không tìm thấy lịch khóa sân."));
        blocks.delete(block);
    }
    private BlockResponse response(CourtBlock block) {
        return new BlockResponse(block.getId(), block.getCourt().getId(), block.getCourt().getName(),
                block.getStartAt(), block.getEndAt(), block.getReason());
    }
}
