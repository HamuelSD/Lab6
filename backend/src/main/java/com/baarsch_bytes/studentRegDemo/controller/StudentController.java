package com.baarsch_bytes.studentRegDemo.controller;
import com.baarsch_bytes.studentRegDemo.dto.CourseResponse;
import com.baarsch_bytes.studentRegDemo.dto.StudentRequest;
import com.baarsch_bytes.studentRegDemo.dto.StudentResponse;
import com.baarsch_bytes.studentRegDemo.model.Course;
import com.baarsch_bytes.studentRegDemo.model.Student;
import com.baarsch_bytes.studentRegDemo.repository.CourseRepository;
import com.baarsch_bytes.studentRegDemo.repository.StudentRepository;
import com.baarsch_bytes.studentRegDemo.validation.StudentValidator;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@RequestMapping("/api/students")
public class StudentController {

    private final StudentRepository repository;
    private final CourseRepository courseRepository;
    private StudentValidator validator;

    public StudentController(StudentRepository repository, CourseRepository courseRepository) {
        this.repository = repository;
        this.courseRepository = courseRepository;
        validator = new StudentValidator();
    }

    // GET all students
    @GetMapping
    public List<StudentResponse> getAll() {
        List<Student> students = repository.findAll();
        return students.stream()
                .map(student -> {
                    StudentResponse response = new StudentResponse();
                    response.setId(student.getId());
                    response.setName(student.getName());
                    response.setMajor(student.getMajor());
                    response.setGpa(student.getGpa());

                    response.setCourses(
                            student.getCourses()
                                    .orElse(Collections.emptySet())
                                    .stream()
                                    .map(Course::getName)
                                    .collect(Collectors.toSet()));
                    return response;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public StudentResponse getStudent(@PathVariable long id) {
        Student student = repository.findById(id).orElse(new Student());

        StudentResponse response = new StudentResponse();
        response.setId(student.getId());
        response.setName(student.getName());
        response.setMajor(student.getMajor());
        response.setGpa(student.getGpa());

        response.setCourses(
                student.getCourses()
                        .orElse(Collections.emptySet())
                        .stream()
                        .map(Course::getName)
                        .collect(Collectors.toSet()));
        return response;
    }

    // POST a new student (The target for Postman testing)
    @PostMapping
    //public ResponseEntity<Student> create(@RequestBody Student student) {
    public ResponseEntity<Map<String, String>> create(@Valid @RequestBody StudentRequest studentRequest) {
        // Lab Logic: You could add validation here to throw an error
        // if GPA > 4.0, giving students a negative test case.

        Student student = new Student();
        student.setName(studentRequest.getName());
        student.setMajor(studentRequest.getMajor());
        student.setGpa(studentRequest.getGpa());
        if (studentRequest.getCourses() != null) {
            List<Course> coursesFromDb =
                    courseRepository.findAllById(studentRequest.getCourses());
            student.setCourses(new HashSet<Course>(coursesFromDb));
        }


        /*
        // Attempt to do manual validation
        if (validator.isValid(student)) {
            repository.save(student);
            return ResponseEntity.ok(student.getName() + " added successfully");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("student failed validation");
*/

        // Spring boot validation takes care of that for you!
        repository.save(student);
        Map<String, String> response = new HashMap<>();
        response.put("message", student.getName() + " added successfully");
        return ResponseEntity.ok(response);
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> update(@Valid
                @RequestBody StudentRequest studentRequest,
                @PathVariable Long id) {
        Student student = repository.findById(id).get();
        student.setName(studentRequest.getName());
        student.setMajor(studentRequest.getMajor());
        student.setGpa(studentRequest.getGpa());
        if (studentRequest.getCourses() != null) {
            List<Course> coursesFromDb =
                    courseRepository.findAllById(studentRequest.getCourses());
            student.setCourses(new HashSet<Course>(coursesFromDb));
        }

        repository.save(student);
        Map<String, String> response = new HashMap<>();
        response.put("message", student.getName() + " updated successfully");
        return ResponseEntity.ok(response);
    }

    // DELETE a student
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}