package vn.edu.crs.registrationservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registrations", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    // Retained for compatibility with databases created in earlier lessons.
    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Long courseId;

    private LocalDateTime ngayDangKy;

    private LocalDateTime registrationTime;

    @Column(nullable = false)
    private String trangThai;

    @Column(nullable = false)
    private String status;
}
