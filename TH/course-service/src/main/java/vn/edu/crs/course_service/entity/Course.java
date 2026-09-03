package vn.edu.crs.course_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tenMonHoc;

    @Column(nullable = false)
    private Integer soTinChi;

    @Column(nullable = false)
    private Integer soChoToiDa;

    private Integer soChoConLai;
}
