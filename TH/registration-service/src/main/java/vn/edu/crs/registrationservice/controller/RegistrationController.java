package vn.edu.crs.registrationservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import vn.edu.crs.registrationservice.dto.RegistrationRequest;
import vn.edu.crs.registrationservice.dto.RegistrationResponse;
import vn.edu.crs.registrationservice.service.RegistrationService;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @PostMapping
    public ResponseEntity<RegistrationResponse> registerCourse(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Name", required = false) String username,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody RegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationService.registerCourse(authenticatedStudentId(userId, role), authenticatedUsername(username), request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<RegistrationResponse>> getMyRegistrations(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        return ResponseEntity.ok(registrationService.getMyRegistrations(authenticatedStudentId(userId, role)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelRegistration(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long id) {
        registrationService.cancelRegistration(authenticatedStudentId(userId, role), id);
        return ResponseEntity.noContent().build();
    }

    private Long authenticatedStudentId(String headerValue, String role) {
        if (!"STUDENT".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Chỉ sinh viên mới được thao tác đăng ký học phần.");
        }
        try {
            return Long.valueOf(headerValue);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Chưa xác thực người dùng!");
        }
    }

    private String authenticatedUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Chưa xác thực người dùng!");
        }
        return username;
    }
}
