package vn.edu.crs.registrationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationResponse {
    private Long id;
    private Long studentId;
    private Long courseId;
    private LocalDateTime ngayDangKy;
    private String trangThai;
}
