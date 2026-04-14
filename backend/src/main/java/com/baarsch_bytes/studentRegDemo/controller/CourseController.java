package com.baarsch_bytes.studentRegDemo.controller;

import com.baarsch_bytes.studentRegDemo.dto.CourseRequest;
import com.baarsch_bytes.studentRegDemo.dto.CourseResponse;
import com.baarsch_bytes.studentRegDemo.exception.NullCourseException;
import com.baarsch_bytes.studentRegDemo.exception.NullStudentException;
import com.baarsch_bytes.studentRegDemo.model.Course;
import com.baarsch_bytes.studentRegDemo.model.Student;
import com.baarsch_bytes.studentRegDemo.repository.CourseRepository;
import com.baarsch_bytes.studentRegDemo.repository.StudentRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


// Cross origin is important to allow Typescript requests to come to this service.  However,
// I don't want to hardcode my localhost into my file, so I set up a CORS allowed-origins in the
// application.properties, so that the cross origin can be handled in configuration.
// additionally the WebConfig class in /config makes the cross origin apply to all Controllers
// without needing specific annotations.
@RestController
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
@RequestMapping("/api/courses")
//@CrossOrigin(origins = "http://localhost:3000")
public class CourseController {
    private final CourseRepository repository;
    StudentRepository studentRepository;

    public CourseController(CourseRepository repository,
                            StudentRepository studentRepository) {
        this.repository = repository;
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public List<CourseResponse> getCourses() {
        List<Course> courses = repository.findAll();
        return courses.stream()
                .map(course -> {
                    CourseResponse response = new CourseResponse();
                    response.setId(course.getId());
                    response.setName(course.getName());
                    response.setInstructor(course.getInstructor());
                    response.setMaxSize(course.getMaxSize());
                    response.setRoom(course.getRoom());
                    response.setRoster(
                            course.getRoster().stream()
                                    .map(Student::getName)
                                    .collect(Collectors.toSet()));
                    return response;
                })
                .collect(Collectors.toList());
    }

    @PostMapping
    //public ResponseEntity<Student> create(@RequestBody Student student) {
    public ResponseEntity<String> create(@Valid @RequestBody CourseRequest request) {

        // translate between the dto from the frontend and the entity
        // ruled by the DB.
        Course course = translateRequestToCourse(request, new Course());
        // add course to db
        repository.save(course);
        return ResponseEntity.ok(course.getName() + " added successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(
            @Valid @RequestBody CourseRequest request
            , @PathVariable Long id) {
        Course course = translateRequestToCourse(request, repository.findById(id).get());
        // add course to db
        repository.save(course);
        return ResponseEntity.ok(course.getName() + " updated successfully");
    }

    @PutMapping("/addStudent/{courseId}")
    public ResponseEntity<String> addStudent(@PathVariable Long courseId, @RequestBody long studentId) {
        try {
        Course course = repository.findById(courseId).orElseThrow(
                NullCourseException::new);
        Student student = studentRepository.findById(studentId).orElseThrow(
                NullStudentException::new);
        if (course.getRoster().size() < course.getMaxSize()) {
            course.getRoster().add(student);
            repository.save(course);
            return ResponseEntity.ok(student.getName() + " added successfully");
        }
        // badRequest returns a Body Builder--this is used to create// a Response entity.
            return ResponseEntity.badRequest().body("Course is full");
        }
        catch (NullCourseException | NullStudentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/removeStudent/{courseId}")
    public ResponseEntity<String> removeStudent(@PathVariable Long courseId, @RequestBody long studentId) {
        try {
            Course course = repository.findById(courseId).orElseThrow(
                    NullCourseException::new);
            Student student = studentRepository.findById(studentId).orElseThrow(
                    NullStudentException::new);
            if (course.getRoster().contains(student)) {
                course.getRoster().remove(student);
                repository.save(course);
                return ResponseEntity.ok(student.getName() + " removed successfully");
            }
            // badRequest returns a Body Builder--this is used to create// a Response entity.
            return ResponseEntity.badRequest().body("Student not in that course");
        }
        catch (NullCourseException | NullStudentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getEnrollment/{courseId}")
    public ResponseEntity<Long> getEnrollment(@PathVariable Long courseId) {
        try {
            Course course = repository.findById(courseId).orElseThrow(NullCourseException::new);
            return ResponseEntity.ok((long)course.getRoster().size());
        }catch (NullCourseException e) {
            // Going to say that if the course cannot be found, we will return -1.
            //It might confuse people--but we'll throw it somewhere in the docs.
            return ResponseEntity.badRequest().body(-1L);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCourse(@PathVariable Long courseId) {
        if (repository.existsById(courseId)) {
            repository.deleteById(courseId);
            return ResponseEntity.ok("Course " + courseId + " deleted successfully");
        }
        return ResponseEntity.badRequest().body("Course not found");
    }

    private Course translateRequestToCourse(CourseRequest request, Course course) {
        course.setName(request.getName());
        course.setInstructor(request.getInstructor());
        course.setMaxSize(request.getMaxSize());
        course.setRoom(request.getRoom());
        if (request.getRoster() != null) {
            List<Student> studentsFromDb =
                    studentRepository.findAllById(request.getRoster());
            course.setRoster(new HashSet<Student>(studentsFromDb));
        }
        return course;
    }

}
