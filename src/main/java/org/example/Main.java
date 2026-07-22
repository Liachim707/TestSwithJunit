import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        // 1. Указать путь к скаченному chromedriver
        System.setProperty("webdriver.chrome.driver", "D:\\Soft\\chromedriver-win64\\chromedriver.exe");

        // 2. Создание объекта драйвер
        WebDriver driver = new ChromeDriver();

        // 3. Выполнение команд
        driver.get("https://demoqa.com/books");

        // Ожидание загрузки таблицы
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("rt-table")));

        // Находим строки таблицы
        List<WebElement> tableRowsIncludingHeader = driver.findElements(By.className("rt-table"));
        List<WebElement> filteredTableRows = tableRowsIncludingHeader.stream()
                .filter(row -> !row.getAttribute("class").contains("-padRow"))
                .collect(Collectors.toList());

        // Проверка, что таблица не пустая
        if (filteredTableRows.isEmpty()) {
            System.out.println("Таблица с книгами не загрузилась!");
            driver.quit();
            return;
        }

        // Выводим заголовки
        final String infoDelimiter = " - ";
        String headerValues = filteredTableRows.get(0)
                .findElements(By.className("rt-resizable-header-content"))
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.joining(infoDelimiter));
        System.out.println(headerValues);

        // Выводим информацию по книгам
        filteredTableRows.stream().skip(1)
                .map(tableRow -> {
                    List<WebElement> imgElements = tableRow.findElements(By.tagName("img"));
                    String imgSrc = imgElements.isEmpty() ? "Нет изображения" : imgElements.get(0).getAttribute("src");

                    List<WebElement> bookLinkElements = tableRow.findElements(By.tagName("a"));
                    String bookLink = bookLinkElements.isEmpty() ? "Нет ссылки" : bookLinkElements.get(0).getAttribute("href");
                    String bookTitle = bookLinkElements.isEmpty() ? "Нет названия" : bookLinkElements.get(0).getText();

                    final int authorCellIndex = 2;
                    String bookAuthor = tableRow.findElements(By.className("rt-td")).get(authorCellIndex).getText();

                    final int publisherCellIndex = 3;
                    String publisherCellXpath = String.format("(.//div)[%d]", publisherCellIndex + 1);
                    String bookPublisher = tableRow.findElement(By.xpath(publisherCellXpath)).getText();

                    return String.join(infoDelimiter, imgSrc, "(" + bookLink, bookTitle + ")", bookAuthor, bookPublisher);
                })
                .forEach(System.out::println);

        // 4. Окончание сессии
        driver.quit();
    }
}
public static void main(String[] args) {
        // Установка пути к драйверу Chrome
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver");

        // Инициализация WebDriver
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();

        try {
            // Открытие страницы с книгами
            driver.get("https://demoqa.com/books");

            // Поиск всех книг на странице
            List<WebElement> bookElements = driver.findElements(By.cssSelector(".rt-tbody .rt-tr-group"));

            if (bookElements.isEmpty()) {
                System.out.println("Книги не найдены на странице.");
            } else {
                System.out.println("Информация по книгам:");
                System.out.println("----------------------------");

                // Вывод информации по каждой книге
                for (WebElement bookElement : bookElements) {
                    String title = bookElement.findElement(By.cssSelector(".rt-td:nth-child(1)")).getText();
                    String author = bookElement.findElement(By.cssSelector(".rt-td:nth-child(2)")).getText();
                    String publisher = bookElement.findElement(By.cssSelector(".rt-td:nth-child(3)")).getText();

                    System.out.println("Название: " + title);
                    System.out.println("Автор: " + author);
                    System.out.println("Издатель: " + publisher);
                    System.out.println("----------------------------");
                }
            }
        } finally {
            // Закрытие браузера
            driver.quit();
        }
    }
