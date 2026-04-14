# Software Testing Lab 6: Selenium

**Test Case & Component Spreadsheet:** [LINK](https://docs.google.com/spreadsheets/d/1X_e13okUaqrtZnlUAxSitChmoN6iu3F020V6wfkgr7I/edit?usp=sharing)

### Apple Silicon Compatibility Fixes
Because I am working on a Mac with apple Silicon, running the provided `amd64` Linux containers natively caused the emulated browser engine to crash immediately with a 500 error. Yikes!

To get the environment running, I updated the test Dockerfile to use an Alpine Linux base image (`maven:3.9.14-eclipse-temurin-25-alpine`) and installed the native ARM64 Chromium package via `apk`. I also had to explicitly define the webdriver path in my Java test files so Selenium wouldn't try to auto-download an incompatible driver. Finally, I resolved a `403 Forbidden` error by adding a `@CrossOrigin` annotation to the backend controllers to allow the frontend to save data. This was after a lot of head scratching!

### Finding My Files
Here is where you can find everything you need:

* **The Test Code:** All of my Selenium test classes (including the CRUD tests for Students and Courses, plus the accessibility tests) are located in the `tests/src/test/java/com/baarsch_bytes/end2end/` directory. 
* **Failure/Success Screenshots:** You can view the screenshots generated during the test runs in `tests/screenshots/`. [Screenshots Folder](https://github.com/HamuelSD/Lab6/tree/master/end2end-tests/screenshots).
* **Backend Source:** The Spring Boot backend code is in `backend/src/main/java/com/baarsch_bytes/studentRegDemo/`. Only major change was the previously mentioned update to get rid of the 403 error issue.
* **Test Reports:** The generated Surefire reports are available in `tests/target/surefire-reports/`.

### Project Overview
This demo project creates an environment for running end-to-end Selenium tests inside a Docker/Podman container. The backend is a standard Maven/Spring Boot application running an in-memory H2 database (exposed on port 8080), while the frontend is built with React, TypeScript, and Vite. 

The full test suite executes through the `testing` compose profile and currently yields:
* **39 tests runs with 2 failures (These failures were a result of codebase bugs)**

### Running the Tests

I use Podman for this project. 

# Build and run the full test suite
podman compose --profile testing up --build
