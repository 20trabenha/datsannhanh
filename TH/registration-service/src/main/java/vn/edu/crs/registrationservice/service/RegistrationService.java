package vn.edu.crs.registrationservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${services.course-service.url}")
    private String courseServiceUrl;

    public RegistrationResponse registerCourse(String username, RegistrationRequest request) {
        try {
            restTemplate.getForObject(courseServiceUrl + "/" + request.getCourseId(), Object.class);
        } catch (Exception e) {
            throw new RuntimeException("Môn học không tồn tại hoặc không hợp lệ!");
        }

        if (registrationRepository.existsByUsernameAndCourseIdAndStatus(username, request.getCourseId(), "SUCCESS")) {
            throw new RuntimeException("Bạn đã đăng ký môn học này rồi!");
        }

        Registration registration = Registration.builder()
                .username(username)
                .courseId(request.getCourseId())
                .registrationTime(LocalDateTime.now())
                .status("SUCCESS")
                .build();

        Registration saved = registrationRepository.save(registration);

        return mapToResponse(saved);
    }

    public List<RegistrationResponse> getMyRegistrations(String username) {
        return registrationRepository.findByUsername(username).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void cancelRegistration(String username, Long registrationId) {
        Registration registration = registrationRepository.findByIdAndUsername(registrationId, username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin đăng ký!"));

        registration.setStatus("CANCELLED");
        registrationRepository.save(registration);
    }

    private RegistrationResponse mapToResponse(Registration registration) {
        return RegistrationResponse.builder()
                .id(registration.getId())
                .username(registration.getUsername())
                .courseId(registration.getCourseId())
                .registrationTime(registration.getRegistrationTime())
                .status(registration.getStatus())
                .build();
    }
}