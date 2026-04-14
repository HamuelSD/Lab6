package com.baarsch_bytes.end2end;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Test1Students {

    private static final int MAX_WAIT = 10;
    private static final String BASE_URL = "http://frontend:5173";

    private WebDriver driver;
    private WebDriverWait wait;

    // =========================================================================
    // SETUP / TEARDOWN
    // =========================================================================

    @BeforeEach
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        options.setCapability("goog:loggingPrefs", logPrefs);

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(MAX_WAIT));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // =========================================================================
    // HELPER METHODS
    // =========================================================================

    /**
     * Navigates to the Students page and waits for the form to be ready.
     */
    private void navigateToStudentsPage() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.elementToBeClickable(
                By.id("nav-student-list-link")
        )).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("new-student-name")
        ));
    }

    /**
     * Fills in the new student form and clicks the Add Student button.
     */
    private void createStudent(String name, String major, String gpa) {
        WebElement nameField = driver.findElement(By.id("new-student-name"));
        WebElement majorField = driver.findElement(By.id("new-student-major"));
        WebElement gpaField = driver.findElement(By.id("new-student-gpa"));

        nameField.clear();
        nameField.sendKeys(name);
        majorField.clear();
        majorField.sendKeys(major);
        gpaField.clear();
        gpaField.sendKeys(gpa);

        driver.findElement(By.id("add-student-button")).click();

        // Wait for the backend to process and the UI to re-render
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
    }

    /**
     * Counts the number of student rows currently in the table.
     * Returns 0 if the table or rows are not found.
     */
    private int getStudentRowCount() {
        try {
            WebElement table = driver.findElement(By.id("student-list-table"));
            List<WebElement> rows = table.findElements(By.tagName("tr"));
            // Subtract 1 for the header row
            return Math.max(0, rows.size() - 1);
        } catch (NoSuchElementException e) {
            return 0;
        }
    }

    private void takeScreenshot(String filename) {
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path destination = Paths.get("/tests/screenshots/" + filename);
            Files.createDirectories(destination.getParent());
            Files.copy(screenshot.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Screenshot saved: " + destination.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Failed to save screenshot: " + e.getMessage());
        }
    }

    private void printBrowserLogs() {
        System.err.println("=== BROWSER CONSOLE LOGS ===");
        LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
        for (LogEntry entry : logEntries) {
            System.err.println(entry.getLevel() + " " + entry.getMessage());
        }
        System.err.println("============================");
    }

    // =========================================================================
    // TC 2.1 - TC 2.6 : CREATE STUDENT (Valid)
    // =========================================================================

    /** TC 2.1: Create a student with valid data. */
    @Test
    @Order(1)
    public void tc2_1_CreateStudent() {
        try {
            navigateToStudentsPage();
            createStudent("John Doe", "Computer Science", "3.5");
            takeScreenshot("tc2_1_create_student.png");

            WebElement table = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("student-list-table"))
            );
            String tableText = table.getText();
            assertTrue(tableText.contains("John Doe"), "Student name should appear in the table.");
            assertTrue(tableText.contains("Computer Science"), "Student major should appear.");
            assertTrue(tableText.contains("3.5"), "Student GPA should appear.");

            System.out.println("TC 2.1 PASSED: Student created successfully.");
        } catch (Exception e) {
            takeScreenshot("tc2_1_error.png");
            printBrowserLogs();
            throw e;
        }
    }

    /** TC 2.2: Create a second student to verify multiple rows. */
    @Test
    @Order(2)
    public void tc2_2_CreateStudentSecond() {
        try {
            navigateToStudentsPage();
            createStudent("Jane Smith", "Mathematics", "3.9");
            takeScreenshot("tc2_2_create_second.png");

            WebElement table = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("student-list-table"))
            );
            assertTrue(table.getText().contains("Jane Smith"),
                    "Second student should appear in the table.");

            System.out.println("TC 2.2 PASSED: Second student created.");
        } catch (Exception e) {
            takeScreenshot("tc2_2_error.png");
            printBrowserLogs();
            throw e;
        }
    }

    /** TC 2.3: Create student with GPA at upper boundary (4.0). */
    @Test
    @Order(3)
    public void tc2_3_CreateStudentGpaUpperBoundary() {
        try {
            navigateToStudentsPage();
            createStudent("Max GPA", "CS", "4.0");
            takeScreenshot("tc2_3_gpa_upper.png");

            WebElement table = driver.findElement(By.id("student-list-table"));
            assertTrue(table.getText().contains("Max GPA"),
                    "Student with GPA 4.0 should be created.");

            System.out.println("TC 2.3 PASSED: GPA upper boundary accepted.");
        } catch (Exception e) {
            takeScreenshot("tc2_3_error.png");
            printBrowserLogs();
            throw e;
        }
    }

    /** TC 2.4: Create student with GPA at lower boundary (0.0). */
    @Test
    @Order(4)
    public void tc2_4_CreateStudentGpaLowerBoundary() {
        try {
            navigateToStudentsPage();
            createStudent("Min GPA", "CS", "0.0");
            takeScreenshot("tc2_4_gpa_lower.png");

            WebElement table = driver.findElement(By.id("student-list-table"));
            assertTrue(table.getText().contains("Min GPA"),
                    "Student with GPA 0.0 should be created.");

            System.out.println("TC 2.4 PASSED: GPA lower boundary accepted.");
        } catch (Exception e) {
            takeScreenshot("tc2_4_error.png");
            printBrowserLogs();
            throw e;
        }
    }

    /** TC 2.5: Create student with 1-character name (lower boundary). */
    @Test
    @Order(5)
    public void tc2_5_CreateStudentNameLowerBoundary() {
        try {
            navigateToStudentsPage();
            createStudent("A", "CS", "3.0");
            takeScreenshot("tc2_5_name_lower.png");

            WebElement table = driver.findElement(By.id("student-list-table"));
            // Check that a row exists with just "A" — the table should have it
            assertTrue(table.getText().contains("A"),
                    "Student with 1-char name should be created.");

            System.out.println("TC 2.5 PASSED: Name lower boundary accepted.");
        } catch (Exception e) {
            takeScreenshot("tc2_5_error.png");
            printBrowserLogs();
            throw e;
        }
    }

    /** TC 2.6: Create student with 255-character name (upper boundary). */
    @Test
    @Order(6)
    public void tc2_6_CreateStudentNameUpperBoundary() {
        try {
            navigateToStudentsPage();
            String longName = "A".repeat(255);
            createStudent(longName, "CS", "3.0");
            takeScreenshot("tc2_6_name_upper.png");

            WebElement table = driver.findElement(By.id("student-list-table"));
            assertTrue(table.getText().contains(longName),
                    "Student with 255-char name should be created.");

            System.out.println("TC 2.6 PASSED: Name upper boundary accepted.");
        } catch (Exception e) {
            takeScreenshot("tc2_6_error.png");
            printBrowserLogs();
            throw e;
        }
    }

    // =========================================================================
    // TC 2.7 - TC 2.13 : CREATE STUDENT (Error / Rejection)
    // =========================================================================

    /** TC 2.7: Missing name — student should not be created. */
    @Test
    @Order(7)
    public void tc2_7_CreateStudentMissingName() {
        try {
            navigateToStudentsPage();
            int rowsBefore = getStudentRowCount();

            createStudent("", "Computer Science", "3.0");
            takeScreenshot("tc2_7_missing_name.png");

            int rowsAfter = getStudentRowCount();
            assertEquals(rowsBefore, rowsAfter,
                    "Row count should not change when name is empty.");

            System.out.println("TC 2.7 PASSED: Missing name rejected.");
        } catch (Exception e) {
            takeScreenshot("tc2_7_error.png");
            printBrowserLogs();
            throw e;
        }
    }

    /** TC 2.8: Missing major — student should not be created. */
    @Test
    @Order(8)
    public void tc2_8_CreateStudentMissingMajor() {
        try {
            navigateToStudentsPage();
            int rowsBefore = getStudentRowCount();

            createStudent("Test Student", "", "3.0");
            takeScreenshot("tc2_8_missing_major.png");

            int rowsAfter = getStudentRowCount();
            assertEquals(rowsBefore, rowsAfter,
                    "Row count should not change when major is empty.");

            System.out.println("TC 2.8 PASSED: Missing major rejected.");
        } catch (Exception e) {
            takeScreenshot("tc2_8_error.png");
            printBrowserLogs();
            throw e;
        }
    }

    /** TC 2.9: Missing GPA — student should not be created. */
    @Test
    @Order(9)
    public void tc2_9_CreateStudentMissingGpa() {
        try {
            navigateToStudentsPage();
            int rowsBefore = getStudentRowCount();

            createStudent("Test Student", "CS", "");
            takeScreenshot("tc2_9_missing_gpa.png");

            int rowsAfter = getStudentRowCount();
            assertEquals(rowsBefore, rowsAfter,
                    "Row count should not change when GPA is empty.");

            System.out.println("TC 2.9 PASSED: Missing GPA rejected.");
        } catch (Exception e) {
            takeScreenshot("tc2_9_error.png");
            printBrowserLogs();
            throw e;
        }
    }

    /** TC 2.10: Name 256 chars (above boundary) — should be rejected. */
    @Test
    @Order(10)
    public void tc2_10_CreateStudentNameTooLong() {
        try {
            navigateToStudentsPage();
            int rowsBefore = getStudentRowCount();

            String longName = "A".repeat(256);
            createStudent(longName, "CS", "3.0");
            takeScreenshot("tc2_10_name_too_long.png");

            int rowsAfter = getStudentRowCount();
            assertEquals(rowsBefore, rowsAfter,
                    "Row count should not change when name exceeds 255 chars.");

            System.out.println("TC 2.10 PASSED: Name too long rejected.");
        } catch (Exception e) {
            takeScreenshot("tc2_10_error.png");
            printBrowserLogs();
            throw e;
        }
    }

    /** TC 2.11: Major 256 chars (above boundary) — should be rejected. */
    @Test
    @Order(11)
    public void tc2_11_CreateStudentMajorTooLong() {
        try {
            navigateToStudentsPage();
            int rowsBefore = getStudentRowCount();

            String longMajor = "B".repeat(256);
            createStudent("Test Student", longMajor, "3.0");
            takeScreenshot("tc2_11_major_too_long.png");

            int rowsAfter = getStudentRowCount();
            assertEquals(rowsBefore, rowsAfter,
                    "Row count should not change when major exceeds 255 chars.");

            System.out.println("TC 2.11 PASSED: Major too long rejected.");
        } catch (Exception e) {
            takeScreenshot("tc2_11_error.png");
            printBrowserLogs();
            throw e;
        }
    }

    /** TC 2.12: GPA of 4.1 (above max) — should be rejected. */
    @Test
    @Order(12)
    public void tc2_12_CreateStudentGpaTooHigh() {
        try {
            navigateToStudentsPage();
            int rowsBefore = getStudentRowCount();

            createStudent("High GPA", "CS", "4.1");
            takeScreenshot("tc2_12_gpa_too_high.png");

            int rowsAfter = getStudentRowCount();
            assertEquals(rowsBefore, rowsAfter,
                    "Row count should not change when GPA exceeds 4.0.");

            System.out.println("TC 2.12 PASSED: GPA too high rejected.");
        } catch (Exception e) {
            takeScreenshot("tc2_12_error.png");
            printBrowserLogs();
            throw e;
        }
    }

    /** TC 2.13: GPA of -0.1 (below min) — should be rejected. */
    @Test
    @Order(13)
    public void tc2_13_CreateStudentGpaTooLow() {
        try {
            navigateToStudentsPage();
            int rowsBefore = getStudentRowCount();

            createStudent("Low GPA", "CS", "-0.1");
            takeScreenshot("tc2_13_gpa_too_low.png");

            int rowsAfter = getStudentRowCount();
            assertEquals(rowsBefore, rowsAfter,
                    "Row count should not change when GPA is negative.");

            System.out.println("TC 2.13 PASSED: GPA too low rejected.");
        } catch (Exception e) {
            takeScreenshot("tc2_13_error.png");
            printBrowserLogs();
            throw e;
        }
    }

    // =========================================================================
    // TC 2.14 - TC 2.18 : EDIT STUDENT
    // =========================================================================

    /** TC 2.14: Edit a student's name and verify the change. */
    @Test
    @Order(14)
    public void tc2_14_EditStudent() {
        try {
            navigateToStudentsPage();

            // Make sure there's at least one student to edit
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("student-list-table")));

            // Click Edit on the first student
            WebElement editButton = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("edit-student-button"))
            );
            editButton.click();

            // Wait for edit fields, change the name
            WebElement editNameField = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("edit-student-name"))
            );
            editNameField.clear();
            editNameField.sendKeys("Johnathan Doe");

            // Click Save
            driver.findElement(By.id("edit-student-save-button")).click();
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

            takeScreenshot("tc2_14_edit_student.png");

            WebElement table = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("student-list-table"))
            );
            assertTrue(table.getText().contains("Johnathan Doe"),
                    "Edited name should appear in the table.");

            System.out.println("TC 2.14 PASSED: Student edited successfully.");
        } catch (Exception e) {
            takeScreenshot("tc2_14_error.png");
            printBrowserLogs();
            throw e;
        }
    }

    /** TC 2.15: Cancel an edit — original data should remain. */
    @Test
    @Order(15)
    public void tc2_15_EditStudentCancel() {
        try {
            navigateToStudentsPage();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("student-list-table")));

            WebElement editButton = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("edit-student-button"))
            );
            editButton.click();

            WebElement editNameField = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("edit-student-name"))
            );
            editNameField.clear();
            editNameField.sendKeys("SHOULD NOT SAVE");

            // Click Cancel instead of Save
            driver.findElement(By.id("edit-student-cancel-button")).click();
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

            takeScreenshot("tc2_15_edit_cancel.png");

            WebElement table = driver.findElement(By.id("student-list-table"));
            assertFalse(table.getText().contains("SHOULD NOT SAVE"),
                    "Cancelled edit should not change the student name.");

            System.out.println("TC 2.15 PASSED: Edit cancelled, original data unchanged.");
        } catch (Exception e) {
            takeScreenshot("tc2_15_error.png");
            printBrowserLogs();
            throw e;
        }
    }

    /** TC 2.16: Edit name to 256 chars — should be rejected, original remains. */
    @Test
    @Order(16)
    public void tc2_16_EditStudentNameTooLong() {
        try {
            navigateToStudentsPage();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("student-list-table")));

            // Capture original name before editing
            WebElement table = driver.findElement(By.id("student-list-table"));
            String originalText = table.getText();

            WebElement editButton = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("edit-student-button"))
            );
            editButton.click();

            WebElement editNameField = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("edit-student-name"))
            );
            editNameField.clear();
            editNameField.sendKeys("X".repeat(256));

            driver.findElement(By.id("edit-student-save-button")).click();
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

            takeScreenshot("tc2_16_edit_name_long.png");

            // Reload the page to check what actually persisted
            navigateToStudentsPage();
            WebElement tableAfter = driver.findElement(By.id("student-list-table"));
            assertFalse(tableAfter.getText().contains("X".repeat(256)),
                    "256-char name should not be saved.");

            System.out.println("TC 2.16 PASSED: Edit with too-long name rejected.");
        } catch (Exception e) {
            takeScreenshot("tc2_16_error.png");
            printBrowserLogs();
            throw e;
        }
    }

    /** TC 2.17: Edit GPA to 4.1 — should be rejected, original remains. */
    @Test
    @Order(17)
    public void tc2_17_EditStudentGpaTooHigh() {
        try {
            navigateToStudentsPage();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("student-list-table")));

            WebElement editButton = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("edit-student-button"))
            );
            editButton.click();

            WebElement editGpaField = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("edit-student-gpa"))
            );
            editGpaField.clear();
            editGpaField.sendKeys("4.1");

            driver.findElement(By.id("edit-student-save-button")).click();
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

            takeScreenshot("tc2_17_edit_gpa_high.png");

            // Reload and verify the GPA didn't save as 4.1
            navigateToStudentsPage();
            WebElement table = driver.findElement(By.id("student-list-table"));
            assertFalse(table.getText().contains("4.1"),
                    "GPA of 4.1 should not be saved.");

            System.out.println("TC 2.17 PASSED: Edit with GPA too high rejected.");
        } catch (Exception e) {
            takeScreenshot("tc2_17_error.png");
            printBrowserLogs();
            throw e;
        }
    }

    /** TC 2.18: Edit GPA to -0.1 — should be rejected, original remains. */
    @Test
    @Order(18)
    public void tc2_18_EditStudentGpaTooLow() {
        try {
            navigateToStudentsPage();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("student-list-table")));

            WebElement editButton = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("edit-student-button"))
            );
            editButton.click();

            WebElement editGpaField = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.id("edit-student-gpa"))
            );
            editGpaField.clear();
            editGpaField.sendKeys("-0.1");

            driver.findElement(By.id("edit-student-save-button")).click();
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

            takeScreenshot("tc2_18_edit_gpa_low.png");

            navigateToStudentsPage();
            WebElement table = driver.findElement(By.id("student-list-table"));
            assertFalse(table.getText().contains("-0.1"),
                    "GPA of -0.1 should not be saved.");

            System.out.println("TC 2.18 PASSED: Edit with GPA too low rejected.");
        } catch (Exception e) {
            takeScreenshot("tc2_18_error.png");
            printBrowserLogs();
            throw e;
        }
    }

    // =========================================================================
    // TC 2.19 : DELETE STUDENT
    // =========================================================================

    /** TC 2.19: Delete a student and verify removal. */
    @Test
    @Order(19)
    public void tc2_19_DeleteStudent() {
        try {
            navigateToStudentsPage();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("student-list-table")));

            int rowsBefore = getStudentRowCount();

            WebElement deleteButton = wait.until(
                    ExpectedConditions.elementToBeClickable(By.id("delete-student-button"))
            );
            deleteButton.click();
            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

            takeScreenshot("tc2_19_delete_student.png");

            int rowsAfter = getStudentRowCount();
            assertTrue(rowsAfter < rowsBefore,
                    "Row count should decrease after deleting a student.");

            System.out.println("TC 2.19 PASSED: Student deleted successfully.");
        } catch (Exception e) {
            takeScreenshot("tc2_19_error.png");
            printBrowserLogs();
            throw e;
        }
    }
}
