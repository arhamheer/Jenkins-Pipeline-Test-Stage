package com.arham;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginTest {

    @Test
    void test_login_with_incorrect_credentials() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu");
        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("http://103.139.122.250:4000/");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            // Wait for any input field to appear, then find email/password by type
            WebElement emailField = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input[type='email'], input[type='text']")
                )
            );
            emailField.sendKeys("wrong@test.com");

            WebElement passwordField = driver.findElement(
                By.cssSelector("input[type='password']")
            );
            passwordField.sendKeys("wrongpassword123");

            WebElement submitBtn = driver.findElement(
                By.cssSelector("button[type='submit']")
            );
            submitBtn.click();

            // Wait for error response
            Thread.sleep(3000);

            String pageSource = driver.getPageSource().toLowerCase();
            assertTrue(
                pageSource.contains("invalid") ||
                pageSource.contains("incorrect") ||
                pageSource.contains("error") ||
                pageSource.contains("wrong") ||
                pageSource.contains("failed") ||
                pageSource.contains("credentials"),
                "Expected login error message on page"
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }
    }
}