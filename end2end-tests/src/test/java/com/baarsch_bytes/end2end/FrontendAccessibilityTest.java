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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FrontendAccessibilityTest {

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

        // 2. Initialize the WebDriver
        driver = new ChromeDriver(options);
    }

    @Test
    public void testFrontendIsAccessible() {
        // 3. Navigate to the frontend using the Docker Compose service name
        driver.get("http://frontend:5173/index.html");

        // 3.5 Take Screenshot
        takeScreenshot("frontend-load-success.png");

        // 4. Assertions to prove the page actually loaded
        String pageTitle = driver.getTitle();

        // Ensure the title isn't completely missing (which happens on "Site not reached" errors)
        assertNotNull(pageTitle, "The page title should not be null.");

        // Ensure the browser actually rendered an HTML document
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("<html"), "The DOM should contain standard HTML tags.");

        // Print a success message to the terminal logs
        System.out.println("Success! The page loaded with title: " + pageTitle);
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