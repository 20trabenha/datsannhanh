package vn.edu.crs.course_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import vn.edu.crs.course_service.entity.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Page<Course> findByTenMonHocContainingIgnoreCase(String keyword, Pageable pageable);

    boolean existsByTenMonHocIgnoreCase(String tenMonHoc);

    boolean existsByTenMonHocIgnoreCaseAndIdNot(String tenMonHoc, Long id);
}
