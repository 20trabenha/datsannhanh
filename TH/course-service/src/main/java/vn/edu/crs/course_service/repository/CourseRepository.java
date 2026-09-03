package vn.edu.crs.course_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import vn.edu.crs.course_service.entity.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Page<Course> findByTenMonHocContainingIgnoreCase(String keyword, Pageable pageable);

    boolean existsByTenMonHocIgnoreCase(String tenMonHoc);

    boolean existsByTenMonHocIgnoreCaseAndIdNot(String tenMonHoc, Long id);

    @Modifying
    @Query("update Course c set c.soChoConLai = c.soChoConLai - 1 where c.id = :id and c.soChoConLai > 0")
    int reserveSeatIfAvailable(@Param("id") Long id);

    @Modifying
    @Query("update Course c set c.soChoConLai = c.soChoConLai + 1 where c.id = :id and c.soChoConLai < c.soChoToiDa")
    int releaseSeatIfPossible(@Param("id") Long id);
}
