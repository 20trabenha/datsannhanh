package vn.edu.crs.registrationservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Long courseId;

    private LocalDateTime registrationTime;

    @Column(nullable = false)
    private String status;
}