package vn.edu.crs.registrationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.crs.registrationservice.entity.Registration;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByStudentIdAndTrangThaiOrderByNgayDangKyDesc(Long studentId, String trangThai);
    boolean existsByStudentIdAndCourseIdAndTrangThai(Long studentId, Long courseId, String trangThai);
    Optional<Registration> findByStudentIdAndCourseId(Long studentId, Long courseId);
    Optional<Registration> findByIdAndStudentIdAndTrangThai(Long id, Long studentId, String trangThai);
}
