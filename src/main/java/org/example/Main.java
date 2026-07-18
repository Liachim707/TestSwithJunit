package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) {
        // 1. Указать путь к скаченному chromedriver: можно через PATH или Java Properties
        System.setProperty("webdriver.chrome.driver", "D:\\Soft\\chromedriver-win64\\chromedriver.exe");

        // 2. Создание объекта драйвер, который будет общаться с chromedriver
        WebDriver driver = new ChromeDriver();

        // 3. Выполнение команд
        driver.get("https://demoqa.com/books");

        // находим строки таблицы с информацией по книгам (включая строку с названиями столбцов - table header);
        // исключаем из них пустые строки
        List<WebElement> tableRowsIncludingHeader = driver.findElements(By.className("rt-tr"));
        List<WebElement> filteredTableRows = tableRowsIncludingHeader.stream()
                .filter(row -> !row.getAttribute("class").contains("-padRow"))
                .toList();

        // выводим на экран содержание строк таблицы;
        // начнём с table header
        final String infoDelimiter = " - ";

        String headerValues = filteredTableRows.get(0)
                .findElements(By.className("rt-resizable-header-content"))
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.joining(infoDelimiter));

        System.out.println(headerValues);

        // перейдём к информации по книгам
        filteredTableRows.stream().skip(1)
                .map(tableRow -> {
                    String imgSrc = tableRow.findElement(By.tagName("img")).getAttribute("src");

                    WebElement bookCell = tableRow.findElement(By.tagName("a"));
                    String bookLink = bookCell.getAttribute("href");
                    String bookTitle = bookCell.getText();

                    // ячейки с автором и издателем мы можем идентифицировать только по имени тега и индексу
                    final int authorCellIndex = 2;
                    String bookAuthor = tableRow.findElements(By.className("rt-td")).get(authorCellIndex).getText();

                    final int publisherCellIndex = 3;

                    // более хитрый способ найти по индексу; важно указывать КОНТЕКСТ!
                    String publisherCellXpath = String.format("(.//div)[%d]", publisherCellIndex + 1);
                    String bookPublisher = tableRow.findElement(By.xpath(publisherCellXpath)).getText();

                    // соединяем всю полученную информацию в одну строку
                    String tableRowInfo = String.join(infoDelimiter, imgSrc, "(" + bookLink, bookTitle + ")", bookAuthor, bookPublisher);
                    return tableRowInfo;
                })
                .forEach(System.out::println);

        // 4. Окончание сессии
        driver.quit();
        //driver.close() - закрывает только текущее окно; если оно единственно открытое, то заканчивается и сессия
    }

    public class MainClass {

        public static void main(String[] args) {
            WebDriver driver = configureAndCreateDriver();
            printAllFullBookInfoToTerminal(driver);
            tearDownDriver(driver);
        }

        public static WebDriver configureAndCreateDriver() {
            System.setProperty("webdriver.chrome.driver", "D:\\Soft\\chromedriver-win64\\chromedriver.exe");
            WebDriver driver = new ChromeDriver();
            driver.manage().window().maximize();
            return driver;
        }

        public static void tearDownDriver(WebDriver driver) {
            if (driver != null) {
                driver.quit();
            }
        }

        public static void printAllFullBookInfoToTerminal(WebDriver driver) {
            List<String> bookUrls = getAllBookUrls(driver);
            for (String bookUrl : bookUrls) {
                printFullBookInfoToTerminal(driver, bookUrl);
                System.out.println("----------------------");
            }
        }

        public static List<String> getAllBookUrls(WebDriver driver) {
            List<String> bookUrls = new ArrayList<>();
            driver.get("https://demoqa.com/books");

            List<WebElement> bookRows = driver.findElements(By.cssSelector(".rt-tbody .rt-tr-group"));
            for (WebElement row : bookRows) {
                WebElement linkElement = row.findElement(By.cssSelector(".action-buttons a"));
                String relativeUrl = linkElement.getAttribute("href");
                bookUrls.add(relativeUrl);
            }
            return bookUrls;
        }

        public static void printFullBookInfoToTerminal(WebDriver driver, String bookUrl) {
            driver.get(bookUrl);
            System.out.println("Информация о книге:");

            List<WebElement> infoElements = driver.findElements(By.cssSelector(".profile-book-info .row"));
            for (WebElement element : infoElements) {
                String label = element.findElement(By.cssSelector(".label")).getText();
                String value = element.findElement(By.cssSelector(".text")).getText();
                System.out.printf("%s: %s%n", label, value);
            }
        }
    }
}