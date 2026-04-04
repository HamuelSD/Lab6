package com.baarsch_bytes.end2end;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
// the followng two imports are for screen shots in Selenium
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

// These are for saving the screen shots after they have been taken.
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

// These are to allow waiting because the test is running faster than the page is
// getting rendered:
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

// And these are to turn on logging, so we have an easier time finding errors
// in the browser logs.  Since we aren't running the Selenium tests on an ACTUAL
// screen, we can't inspect the elements.
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FrontendAccessibilityWaitsTest {

    private final int MAX_WAIT = 10;
    private WebDriver driver;

    @BeforeEach
    public void setUp() {
        // 1. Configure the Headless Browser for the Linux Container
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox"); // Required for rootless/Docker environments
        options.addArguments("--disable-dev-shm-usage"); // Prevents memory crashes in containers

        // Optional: Set a specific window size so your screenshot looks like
        // a real desktop monitor  Alternatively, perhap test various screen sizes.
        options.addArguments("--window-size=1920,1080");

        // Logging set up
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        options.setCapability("goog:loggingPrefs", logPrefs);

        // 2. Initialize the WebDriver
        driver = new ChromeDriver(options);
    }

    @Test
    public void testFrontendIsAccessible() {
        // 3. Navigate to the frontend using the Docker Compose service name
        driver.get("http://frontend:5173/");

        try {
            // 3.5.5 Wait for the page to load up to the courselist
            // sets a maximum wait time of 10 seconds.
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(MAX_WAIT));
            // next, waits until either the selected element is loaded (new-course-room),
            // or 10 seconds has passed.
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("new-course-room")));


            // 3.5 Take Screenshot
            takeScreenshot("frontend-load-waits-success.png");

            // 4. Assertions to prove the page actually loaded
            String pageTitle = driver.getTitle() + "_waits";

            // Ensure the title isn't completely missing (which happens on "Site not reached" errors)
            assertNotNull(pageTitle, "The page title should not be null.");

            // Ensure the browser actually rendered an HTML document
            String pageSource = driver.getPageSource();
            assertTrue(pageSource.contains("<html"), "The DOM should contain standard HTML tags.");

            // Print a success message to the terminal logs
            System.out.println("Success! The page loaded with title: " + pageTitle);
        } catch (Exception e) {
            // I could use a NoSuchElementException, since that is the specific error
            // I am getting, but I want this to be more general, so that it shows the
            // screenshot of any error.
            takeScreenshot("error-missing-element.png");
            // Let's log errors!
            System.err.println("=== BROWSER CONSOLE LOGS ===");
            LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);
            for (LogEntry entry : logEntries) {
                System.err.println(entry.getLevel() + " " + entry.getMessage());
            }
            System.err.println("============================");
            // Let's stop passing the test just because we caught the exception.
            throw e;
        }
    }

    @AfterEach
    public void tearDown() {
        // 5. Always close the browser to free up container RAM
        if (driver != null) {
            driver.quit();
        }
    }

    private void takeScreenshot(String filename) {
        System.out.println("Attempting Screenshot...");
        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            Path destination = Paths.get("/tests/screenshots/" + filename);

            Files.createDirectories(destination.getParent());
            Files.copy(screenshot.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Screenshot saved to: " + destination.toAbsolutePath());
        }
        catch(IOException e) {
            System.out.println("Failed to save screenshot: " + e.getMessage());
        }
    }

}