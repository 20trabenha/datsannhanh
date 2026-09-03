package vn.edu.crs.registrationservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.edu.crs.registrationservice.dto.RegistrationRequest;
import vn.edu.crs.registrationservice.dto.RegistrationResponse;
import vn.edu.crs.registrationservice.entity.Registration;
import vn.edu.crs.registrationservice.repository.RegistrationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RegistrationService {

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${services.course-service.base-url}")
    private String courseServiceBaseUrl;

    @Transactional
    public RegistrationResponse registerCourse(Long studentId, String username, RegistrationRequest request) {
        if (registrationRepository.existsByStudentIdAndCourseIdAndTrangThai(studentId, request.getCourseId(), "DA_DANG_KY")) {
            throw new IllegalStateException("Sinh viên đã đăng ký môn học này rồi!");
        }

        reserveSeat(request.getCourseId());
        try {
            Registration registration = registrationRepository.findByStudentIdAndCourseId(studentId, request.getCourseId())
                    .orElseGet(() -> Registration.builder().studentId(studentId).username(username).courseId(request.getCourseId()).build());
            registration.setUsername(username);
            registration.setNgayDangKy(LocalDateTime.now());
            registration.setRegistrationTime(registration.getNgayDangKy());
            registration.setTrangThai("DA_DANG_KY");
            registration.setStatus("SUCCESS");
            return mapToResponse(registrationRepository.save(registration));
        } catch (RuntimeException exception) {
            releaseSeatQuietly(request.getCourseId());
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<RegistrationResponse> getMyRegistrations(Long studentId) {
        return registrationRepository.findByStudentIdAndTrangThaiOrderByNgayDangKyDesc(studentId, "DA_DANG_KY").stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelRegistration(Long studentId, Long registrationId) {
        Registration registration = registrationRepository.findByIdAndStudentIdAndTrangThai(registrationId, studentId, "DA_DANG_KY")
                .orElseThrow(() -> new java.util.NoSuchElementException("Không tìm thấy thông tin đăng ký!"));

        releaseSeat(registration.getCourseId());
        try {
            registration.setTrangThai("DA_HUY");
            registration.setStatus("CANCELLED");
            registrationRepository.save(registration);
        } catch (RuntimeException exception) {
            reserveSeatQuietly(registration.getCourseId());
            throw exception;
        }
    }

    private void reserveSeat(Long courseId) {
        callCourseSeatEndpoint(courseId, "reserve-seat");
    }

    private void releaseSeat(Long courseId) {
        callCourseSeatEndpoint(courseId, "release-seat");
    }

    private void reserveSeatQuietly(Long courseId) {
        try {
            reserveSeat(courseId);
        } catch (RuntimeException ignored) {
            // The original persistence error is more useful to the caller.
        }
    }

    private void releaseSeatQuietly(Long courseId) {
        try {
            releaseSeat(courseId);
        } catch (RuntimeException ignored) {
            // The original persistence error is more useful to the caller.
        }
    }

    private void callCourseSeatEndpoint(Long courseId, String action) {
        try {
            restTemplate.exchange(courseServiceBaseUrl + "/internal/courses/{id}/" + action,
                    HttpMethod.PATCH, HttpEntity.EMPTY, Void.class, courseId);
        } catch (HttpStatusCodeException exception) {
            String body = exception.getResponseBodyAsString();
            String message = body.replaceFirst("^.*\\\"message\\\"\\s*:\\s*\\\"", "")
                    .replaceFirst("\\\".*$", "");
            throw new IllegalStateException(message.isBlank() ? "Không thể cập nhật chỗ học phần." : message);
        } catch (Exception exception) {
            throw new IllegalStateException("Không thể kết nối Course Service.");
        }
    }

    private RegistrationResponse mapToResponse(Registration registration) {
        return RegistrationResponse.builder()
                .id(registration.getId())
                .studentId(registration.getStudentId())
                .courseId(registration.getCourseId())
                .ngayDangKy(registration.getNgayDangKy())
                .trangThai(registration.getTrangThai())
                .build();
    }
}
