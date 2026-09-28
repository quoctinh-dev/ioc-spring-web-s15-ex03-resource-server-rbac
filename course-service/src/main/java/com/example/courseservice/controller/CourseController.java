package com.example.courseservice.controller;

import com.example.courseservice.dto.CourseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final List<CourseDto> courses = new ArrayList<>(List.of(
            new CourseDto(1L, "Lập trình Java Spring Boot Microservices", "Nguyễn Văn A"),
            new CourseDto(2L, "Thiết lập API Gateway & Spring Security", "Trần Thị B")
    ));

    // API 1: Xem danh sách khóa học (STUDENT hoặc INSTRUCTOR đều vào được)
    @GetMapping
    @PreAuthorize("hasAuthority('STUDENT') or hasAuthority('INSTRUCTOR')")
    public ResponseEntity<List<CourseDto>> getAllCourses() {
        return ResponseEntity.ok(courses);
    }

    // API 2: Tạo khóa học mới (Chỉ INSTRUCTOR mới vào được)
    @PostMapping
    @PreAuthorize("hasAuthority('INSTRUCTOR')")
    public ResponseEntity<CourseDto> createCourse(@RequestBody CourseDto courseDto) {
        courseDto.setId((long) (courses.size() + 1));
        courses.add(courseDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(courseDto);
    }
}