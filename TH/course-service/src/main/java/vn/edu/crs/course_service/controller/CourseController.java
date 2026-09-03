package vn.edu.crs.course_service.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.crs.course_service.dto.CourseDTO;
import vn.edu.crs.course_service.service.CourseService;


@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @GetMapping
    public ResponseEntity<Page<CourseDTO>> getAllCourses(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return ResponseEntity.ok(courseService.getCourses(keyword, PageRequest.of(safePage, safeSize)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseDTO dto) {
        CourseDTO created = courseService.createCourse(dto);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDTO dto) {
        return ResponseEntity.ok(courseService.updateCourse(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}
