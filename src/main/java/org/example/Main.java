import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Main {

    public static void main(String[] args) {
        // Укажите путь к установленному ChromeDriver.
        System.setProperty("webdriver.chrome.driver",
                "D:\\Soft\\chromedriver-win64\\chromedriver.exe");

        WebDriver driver = new ChromeDriver();

        try {
            driver.manage().window().maximize();
            driver.get("https://demoqa.com/books");

            // Ждём появления строк таблицы с книгами.
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".rt-tbody .rt-tr-group")));

            // Получаем строки таблицы. Пустые строки пропускаем.
            List<WebElement> bookRows = driver.findElements(
                    By.cssSelector(".rt-tbody .rt-tr-group"));

            System.out.println("Информация по книгам:");
            System.out.println("----------------------------");

            int booksPrinted = 0;

            for (WebElement row : bookRows) {
                List<WebElement> cells = row.findElements(By.cssSelector(".rt-td"));

                // В таблице четыре столбца: изображение, название, автор, издатель.
                if (cells.size() < 4 || cells.get(1).getText().isBlank()) {
                    continue;
                }

                String title = cells.get(1).getText();
                String author = cells.get(2).getText();
                String publisher = cells.get(3).getText();

                List<WebElement> links = cells.get(1).findElements(By.tagName("a"));
                String bookUrl = links.isEmpty()
                        ? "Ссылка отсутствует"
                        : links.get(0).getAttribute("href");

                System.out.println("Название: " + title);
                System.out.println("Автор: " + author);
                System.out.println("Издатель: " + publisher);
                System.out.println("Ссылка: " + bookUrl);
                System.out.println("----------------------------");

                booksPrinted++;
            }

            if (booksPrinted == 0) {
                System.out.println("Книги не найдены на странице.");
            } else {
                System.out.println("Всего выведено книг: " + booksPrinted);
            }
        } finally {
            // Закрываем браузер даже в случае ошибки.
            driver.quit();
        }
    }
}
