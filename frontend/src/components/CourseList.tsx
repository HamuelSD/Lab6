import React, { useEffect, useState } from 'react';
import { CourseResponse } from '../types/student-reg-types';
import { CourseService } from '../services/CourseService';
import { StudentService } from '../services/StudentService';

const CourseList: React.FC = () => {
    // State to store our courses
    const [courses, setCourses] = useState<CourseResponse[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [newCourseName, setNewCourseName] = useState("");
    const [newCourseInstructor, setNewCourseInstructor] = useState("");
    const [newCourseMaxSize, setNewCourseMaxSize] = useState("");
    const [newCourseRoom, setNewCourseRoom] = useState("");
    const [editingCourseId, setEditingCourseId] = useState<number | null>(null);
    const [editForm, setEditorForm] = useState({
        name: "",
        instructor: "",
        maxSize: "",
        room: ""
    });
    const [studentsToRemove, setStudentsToRemove] = useState<Record<number, string>>({});
    const [allStudents, setAllStudents] = useState<any[]>([]);
    // Keeps track of the selected student for each row.
    // Example: { 1: "105", 2: "108" } -> Course 1 selected Student 105
    const [selectedStudents, setSelectedStudents] = useState<Record<number, string>>({});

    // The fetch data function.
    const loadCourses = async () => {
        try {
            const data = await CourseService.getAllCourses();
            setCourses(data);
            const studentData = await StudentService.getAllStudents();
            setAllStudents(studentData);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'An error occurred');
        } finally {
            setLoading(false);
        }
    };

    const handleAddStudent = async (courseId: number) => {
        // Look up the selected student ID for this specific course row
        const studentIdString = selectedStudents[courseId];

        if (!studentIdString) {
            alert("Please select a student first.");
            return;
        }

        try {
            const studentId = parseInt(studentIdString);

            // Call your Service
            await CourseService.addStudentToCourse(courseId, studentId);

            // Refresh the table to show the new roster
            await loadCourses();

            // Optional: Reset the dropdown for this row back to default
            setSelectedStudents({ ...selectedStudents, [courseId]: "" });
        } catch (err) {
            console.error("Failed to add student to course:", err);
        }
    };

    const handleRemoveStudent = async (courseId: number) => {
        const studentIdString = studentsToRemove[courseId];

        if (!studentIdString) {
            alert("Please select a student to remove.");
            return;
        }

        try {
            const studentId = parseInt(studentIdString);

            // Assuming you make a matching method in your CourseService
            await CourseService.removeStudentFromCourse(courseId, studentId);

            await loadCourses();

            // Reset the dropdown for this row
            setStudentsToRemove({ ...studentsToRemove, [courseId]: "" });
        } catch (err) {
            console.error("Failed to remove student:", err);
        }
    };

    const handleEditClick = (course: any) => {
        setEditingCourseId(course.id);
        setEditorForm({
            name: course.name || "",
            instructor: course.instructor ? course.instructor.toString() : "",
            maxSize: course.maxSize? course.maxSize.toString() : "",
            room: course.room || ""
        });
    };

    const handleSaveEdit = async (id:number) => {
        try {
            const updatedData = {
                name: editForm.name,
                instructor: parseInt(editForm.instructor),
                maxSize: parseInt(editForm.maxSize),
                room: editForm.room
            };

            // Send PUT request to your Spring Boot controller
            await CourseService.updateCourse(id, updatedData);

            // Close the edit row and refresh the list
            setEditingCourseId(null);
            await loadCourses();
        } catch (err) {
            console.error("Failed to update course: ", err);
        }
    };

    const handleDelete = async (id: number) => {
        try {
            await CourseService.deleteCourse(id);
            // This triggers a re-render without a page refresh!
            setCourses(courses.filter(c => c.id !== id));
        } catch (err) {
            alert("Delete failed!");
        }
    };

    const handleAdd = async () => {
        // inputs always want to handle strings, so convert the max size to a number
        const numericSize = parseInt(newCourseMaxSize.trim());
        const numericInstructor = parseInt(newCourseInstructor.trim());
        // try to save the course.
        const savedCourse = await CourseService.addCourse(
            { name: newCourseName,
            instructor: numericInstructor,
            maxSize: numericSize,
            room: newCourseRoom,
            roster: undefined
            });
        console.log("I saved the courses--now, to update the fields.")
        // refresh the list of courses
        await loadCourses();
        //reset all the fields to clear the data

        setNewCourseName(""); // Clear the input
        setNewCourseInstructor("");
        setNewCourseMaxSize("");
        setNewCourseRoom("");

            };

    // Fetch data on component load
    useEffect(() => {
        loadCourses();
    }, []);

    if (loading) return <div>Loading courses from Spring Boot...</div>;
    if (error) return <div style={{ color: 'red' }}>Error: {error}</div>;

    return (

        <div>
            <div id="new-course-fields" className="course-container">
                <input id="new-course-name"
                    value={newCourseName}
                    onChange={(e) => setNewCourseName(e.target.value)}
                    placeholder="New Course Name"
                ></input>
                <input id="new-course-instructor"
                    value={newCourseInstructor}
                    onChange={(e) => setNewCourseInstructor(e.target.value)}
                    placeholder = "Instructor ID Number"
                ></input>
                <input id="new-course-max-size"
                    value ={newCourseMaxSize}
                    onChange={(e) => setNewCourseMaxSize(e.target.value)}
                    placeholder={"MaxSize"}
                ></input>
                <input id="new-course-room"
                    value={newCourseRoom}
                    onChange={(e) => setNewCourseRoom(e.target.value)}
                    placeholder={"Room"}
                ></input>

                <button id = "add-course-button" onClick={handleAdd}>Add Course</button>
            </div>
            <h1>Available Courses</h1>
            <table id = "course-list-table">
                <thead>
                <tr>
                    <th>Course Name</th>
                    <th>Instructor</th>
                    <th>Max Size</th>
                    <th>Room</th>
                    <th>Enrolled Count</th>
                    <th>Roster</th>
                    <th>Delete</th>
                    <th>Edit</th>
                    <th>Add Student</th>
                </tr>
                </thead>
                <tbody>
                {courses.map(course => (
                    <React.Fragment key={course.id}>
                        <tr key={course.id} id = {`course-row-${course.id}`}>
                            <td id = {`course-name-${course.id}`}>{course.name}</td>
                            <td id = {`course-instructor-${course.id}`}>{course.instructor}</td>
                            <td id = {`course-max-size-${course.id}`}>{course.maxSize}</td>
                            <td id = {`course-room-${course.id}`}>{course.room}</td>
                            <td id = {`course-roster-${course.id}`}>{course.roster ? course.roster.length : 0}</td>
                            <td>{course.roster ? course.roster.join(', ') : 'Empty'}</td>
                            <td>
                                <button id = "delete-course-button" onClick={() => handleDelete(course.id)} style={{color: 'red'}}>
                                    Delete
                                </button>
                            </td>
                            <td>
                                <button id = "edit-course-button" onClick={() => handleEditClick(course)} style={{color: 'blue'}}>
                                    Edit
                                </button>
                            </td>
                            <td>
                                <div style={{display: 'flex', gap: '5px'}}>
                                    <select id = "select-student"
                                        value={selectedStudents[course.id] || ""}
                                        onChange={(e) => setSelectedStudents({
                                            ...selectedStudents,
                                            [course.id]: e.target.value
                                        })}
                                    >
                                        <option value="" disabled>Select a Student</option>
                                        {allStudents.map(student => (
                                            <option key={student.id} value={student.id}>
                                                {student.name} {/* Adjust based on your student object */}
                                            </option>
                                        ))}
                                    </select>

                                    <button id = "add-student-button"
                                        onClick={() => handleAddStudent(course.id)}
                                        style={{color: 'green'}}
                                    >
                                        Add Student
                                    </button>
                                </div>
                            </td>
                        </tr>
                        {/* Row 2: The Conditional Edit Row */}
                        {editingCourseId === course.id && (
                            <tr style={{backgroundColor: '#f0f8ff'}}>
                                <td colSpan={7}> {/* Spans across all columns */}
                                    <div style={{display: 'flex', gap: '10px', padding: '10px'}}>
                                        <input id ="edit-course-name"
                                            value={editForm.name}
                                            onChange={(e) =>
                                                setEditorForm({...editForm, name: e.target.value})}
                                            placeholder="Course Name"
                                        />
                                        <input id ="edit-course-instructor"
                                            value={editForm.instructor}
                                            onChange={(e) =>
                                                setEditorForm({...editForm, instructor: e.target.value})}
                            placeholder="Instructor ID"
                        />
                        <input id ="edit-course-max-size"
                            value={editForm.maxSize}
                            onChange={(e) =>
                                setEditorForm({...editForm, maxSize: e.target.value})}
                            placeholder="Max Size"
                        />
                        <input id ="edit-course-room"
                            value={editForm.room}
                            onChange={(e) =>
                                setEditorForm({...editForm, room: e.target.value})}
                            placeholder="Room"
                        />
                        {/* --- REMOVE STUDENT SECTION --- */}
                        <div style={{ display: 'flex', gap: '5px', marginTop: '10px' }}>
                            <select
                                id="remove-student-select" // Selenium ID!
                                value={studentsToRemove[course.id] || ""}
                                onChange={(e) => setStudentsToRemove({
                                    ...studentsToRemove,
                                    [course.id]: e.target.value
                                })}
                            >
                                <option value="" disabled>Remove a Student</option>

                                {/* Filter allStudents to ONLY show those currently in the course roster */}
                                {allStudents
                                    .filter(student => course.roster && course.roster.includes(student.name))
                                    .map(enrolledStudent => (
                                        <option key={enrolledStudent.id} value={enrolledStudent.id}>
                                            {enrolledStudent.name}
                                        </option>
                                    ))
                                }
                            </select>

                            <button
                                id="remove-student-button" // Selenium ID!
                                onClick={() => handleRemoveStudent(course.id)}
                                style={{color: 'red'}}
                            >
                                Remove Student
                            </button>
                        </div>
                        <button id ="edit-course-save-button" onClick={() => handleSaveEdit(course.id)} style={{color: 'green'}}>
                            Save
                        </button>
                        <button id ="edit-course-cancel-button" onClick={() => setEditingCourseId(null)}>
                            Cancel
                        </button>
                    </div>
                </td>
                </tr>
                )}
            </React.Fragment>
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default CourseList;