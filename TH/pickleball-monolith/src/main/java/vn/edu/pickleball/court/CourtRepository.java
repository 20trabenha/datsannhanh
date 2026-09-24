package vn.edu.pickleball.court;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface CourtRepository extends JpaRepository<Court, Long> {
    Page<Court> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Court> findByCategoryId(Long categoryId, Pageable pageable);
    Page<Court> findByCategoryIdAndNameContainingIgnoreCase(Long categoryId, String name, Pageable pageable);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    boolean existsByCategoryId(Long categoryId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Court> findLockedById(Long id);
}
