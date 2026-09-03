package vn.edu.crs.registrationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest {
    // Kept only for backward-compatible clients. The server always uses the ID from the verified token.
    private Long studentId;

    @NotNull(message = "Course ID không được để trống")
    private Long courseId;
}
