import {CourseRequest, StudentResponse, StudentRequest} from "../types/student-reg-types";

const BASE_URL = '/api/students';

export const StudentService = {


    // GET /api/students
    async getAllStudents(): Promise<StudentResponse[]> {
        const response = await fetch(`${BASE_URL}`);
        return response.json();
    },

    // POST /api/students
    async createStudent(student: StudentRequest): Promise<{message: string}> {
        const response = await fetch(`${BASE_URL}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(student)
        });
        return response.json();
    },


    async addStudent(student: Partial<StudentRequest>): Promise<string> {
        const response = await fetch(`${BASE_URL}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(student),
        });
        return response.text();
    },

    async updateStudent(id: number, student: Partial<StudentRequest>): Promise<string> {
        const response = await fetch(`${BASE_URL}/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(student),
        });
        return response.text();
    },

    async deleteStudent(id: number): Promise<void> {
        await fetch(`${BASE_URL}/${id}`, { method: 'DELETE' });
    }
};