package vn.edu.pickleball.court;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface CourtBlockRepository extends JpaRepository<CourtBlock, Long> {
    @Query("select count(b) > 0 from CourtBlock b where b.court.id = :courtId " +
            "and b.startAt < :endAt and b.endAt > :startAt")
    boolean hasOverlap(@Param("courtId") Long courtId, @Param("startAt") LocalDateTime startAt,
                       @Param("endAt") LocalDateTime endAt);
    @Query("select b from CourtBlock b where b.court.id = :courtId " +
            "and b.startAt < :endAt and b.endAt > :startAt order by b.startAt")
    List<CourtBlock> findForPeriod(@Param("courtId") Long courtId, @Param("startAt") LocalDateTime startAt,
                                   @Param("endAt") LocalDateTime endAt);
    List<CourtBlock> findAllByOrderByStartAtDesc();
    List<CourtBlock> findByCourtIdOrderByStartAtDesc(Long courtId);
}
