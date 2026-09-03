package vn.edu.crs.registrationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.crs.registrationservice.entity.Registration;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByUsername(String username);
    boolean existsByUsernameAndCourseIdAndStatus(String username, Long courseId, String status);
    Optional<Registration> findByIdAndUsername(Long id, String username);
}