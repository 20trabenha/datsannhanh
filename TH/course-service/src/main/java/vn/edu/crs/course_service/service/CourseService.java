package vn.edu.crs.course_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.crs.course_service.dto.CourseDTO;
import vn.edu.crs.course_service.entity.Course;
import vn.edu.crs.course_service.repository.CourseRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public Page<CourseDTO> getCourses(String keyword, Pageable pageable) {
        Page<Course> courses = keyword == null || keyword.isBlank()
                ? courseRepository.findAll(pageable)
                : courseRepository.findByTenMonHocContainingIgnoreCase(keyword.trim(), pageable);

        return courses.map(this::mapToDTO);
    }

    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy môn học với ID: " + id));
        return mapToDTO(course);
    }

    public CourseDTO createCourse(CourseDTO dto) {
        String courseName = dto.getTenMonHoc().trim();
        if (courseRepository.existsByTenMonHocIgnoreCase(courseName)) {
            throw new IllegalArgumentException("Tên môn học đã tồn tại!");
        }

        Course course = Course.builder()
                .tenMonHoc(courseName)
                .soTinChi(dto.getSoTinChi())
                .soChoToiDa(dto.getSoChoToiDa())
                .soChoConLai(dto.getSoChoToiDa())
                .build();

        Course saved = courseRepository.save(course);
        return mapToDTO(saved);
    }

    public CourseDTO updateCourse(Long id, CourseDTO dto) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy môn học với ID: " + id));

        String courseName = dto.getTenMonHoc().trim();
        if (courseRepository.existsByTenMonHocIgnoreCaseAndIdNot(courseName, id)) {
            throw new IllegalArgumentException("Tên môn học đã tồn tại!");
        }

        int seatsAlreadyUsed = course.getSoChoToiDa() - course.getSoChoConLai();
        if (dto.getSoChoToiDa() < seatsAlreadyUsed) {
            throw new IllegalStateException("Số chỗ tối đa không thể nhỏ hơn số sinh viên đã đăng ký!");
        }

        course.setTenMonHoc(courseName);
        course.setSoTinChi(dto.getSoTinChi());
        course.setSoChoToiDa(dto.getSoChoToiDa());
        // The form does not edit remaining seats. Preserve enrolled students when capacity changes.
        course.setSoChoConLai(dto.getSoChoToiDa() - seatsAlreadyUsed);

        Course updated = courseRepository.save(course);
        return mapToDTO(updated);
    }

    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy môn học với ID: " + id));
        courseRepository.delete(course);
    }

    // --- Bổ sung 2 hàm xử lý giữ chỗ / nhả chỗ môn học ---

    @Transactional
    public CourseDTO reserveSeat(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new NoSuchElementException("Không tìm thấy môn học với ID: " + id);
        }
        if (courseRepository.reserveSeatIfAvailable(id) == 0) {
            throw new IllegalStateException("Môn học đã hết chỗ trống!");
        }
        return mapToDTO(courseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy môn học với ID: " + id)));
    }

    @Transactional
    public CourseDTO releaseSeat(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new NoSuchElementException("Không tìm thấy môn học với ID: " + id);
        }
        courseRepository.releaseSeatIfPossible(id);
        return mapToDTO(courseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy môn học với ID: " + id)));
    }

    private CourseDTO mapToDTO(Course course) {
        return new CourseDTO(
                course.getId(),
                course.getTenMonHoc(),
                course.getSoTinChi(),
                course.getSoChoToiDa(),
                course.getSoChoConLai()
        );
    }
}
