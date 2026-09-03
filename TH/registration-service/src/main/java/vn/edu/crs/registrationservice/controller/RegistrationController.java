package vn.edu.crs.registrationservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.crs.registrationservice.dto.RegistrationRequest;
import vn.edu.crs.registrationservice.dto.RegistrationResponse;
import vn.edu.crs.registrationservice.service.RegistrationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @PostMapping
    public ResponseEntity<?> registerCourse(
            @RequestHeader(value = "X-User-Name", required = false) String username,
            @RequestBody RegistrationRequest request) {
        if (username == null || username.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa xác thực người dùng!"));
        }

        try {
            RegistrationResponse response = registrationService.registerCourse(username, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/my-courses")
    public ResponseEntity<?> getMyRegistrations(@RequestHeader(value = "X-User-Name", required = false) String username) {
        if (username == null || username.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa xác thực người dùng!"));
        }

        List<RegistrationResponse> list = registrationService.getMyRegistrations(username);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelRegistration(
            @RequestHeader(value = "X-User-Name", required = false) String username,
            @PathVariable Long id) {
        if (username == null || username.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Chưa xác thực người dùng!"));
        }

        try {
            registrationService.cancelRegistration(username, id);
            return ResponseEntity.ok(Map.of("message", "Hủy đăng ký môn học thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}