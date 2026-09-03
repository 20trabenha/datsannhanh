package vn.edu.crs.course_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vn.edu.crs.course_service.entity.Course;
import vn.edu.crs.course_service.repository.CourseRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CourseRepository courseRepository;

    @Override
    public void run(String... args) throws Exception {
        if (courseRepository.count() == 0) {
            courseRepository.save(Course.builder()
                    .tenMonHoc("Lập trình Java")
                    .soTinChi(3)
                    .soChoToiDa(50)
                    .soChoConLai(50)
                    .build());

            courseRepository.save(Course.builder()
                    .tenMonHoc("Cơ sở dữ liệu")
                    .soTinChi(3)
                    .soChoToiDa(40)
                    .soChoConLai(40)
                    .build());
        }
    }
}