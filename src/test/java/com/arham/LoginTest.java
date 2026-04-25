package com.arham;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginTest {

    @Test
    void testLoginWithIncorrectCredentials() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.navigate().to("http://103.139.122.250:4000/");

            driver.findElement(By.cssSelector("input[placeholder='you@comsats.edu.pk']"))
                .sendKeys("qasim@malik.com");
            driver.findElement(By.cssSelector("input[placeholder='••••••••']"))
                .sendKeys("abcdefg");
            driver.findElement(By.xpath("//button[contains(., 'Sign In')]"))
                .click();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            boolean errorVisible = wait.until(browser -> {
                String pageSource = browser.getPageSource();
                return pageSource.contains("Failed to fetch")
                    || pageSource.contains("Incorrect email or password");
            });

            assertTrue(errorVisible);
        } finally {
            driver.quit();
        }
    }
}