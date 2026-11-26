# TestSwithJunit — Movies API tests

## О проекте

Проект содержит автоматизированные интеграционные API-тесты для учебного приложения [Movies](https://github.com/tirzasrwn/movies). Тесты написаны на Java с использованием JUnit 5, OkHttp для HTTP-запросов, Gson для чтения JSON и PostgreSQL JDBC для подготовки и проверки тестовых данных.

## Что проверяют тесты

- **PublicRoutesTest** — получение списка жанров (`GET /v1/genres`) и публичного каталога фильмов (`GET /v1/movies`).
- **AuthRoutesTest** — успешная и неуспешная авторизация, отказ в доступе к admin endpoint без токена, выход из системы (`GET /v1/logout`) и сброс refresh-cookie.
- **AdminRoutesTest** — получение административного каталога, получение фильма по ID, обновление фильма и удаление фильма.
- В тесте удаления фильм создаётся напрямую в PostgreSQL в Arrange, удаляется через admin API, после чего проверяется отсутствие записи в базе.

## Требования

- JDK 17 или новее.
- PostgreSQL с базой данных, настроенной для Movies backend.
- Запущенный Movies backend, доступный по адресу `http://localhost:4000`.
- Gradle Wrapper, который уже находится в репозитории.

## Настройка и запуск

1. Клонируйте репозиторий:

   ```bash
   git clone https://github.com/Liachim707/TestSwithJunit.git
   cd TestSwithJunit
   ```

2. Убедитесь, что PostgreSQL и Movies backend запущены. По умолчанию тесты используют:
   - API: `http://localhost:4000/v1`
   - PostgreSQL: `jdbc:postgresql://localhost:5433/test_database`
   - пользователь БД: `postgres`
   - пароль БД: `PostgreSQL`
   - администратор API: `admin@example.com` / `secret`

3. При необходимости переопределите параметры через переменные окружения:

   | Переменная | Назначение | Значение по умолчанию |
   |---|---|---|
   | `API_BASE_URL` | Базовый URL API, включая `/v1` | `http://localhost:4000/v1` |
   | `DB_URL` | JDBC URL PostgreSQL | `jdbc:postgresql://localhost:5433/test_database` |
   | `DB_USER` | Пользователь PostgreSQL | `postgres` |
   | `DB_PASSWORD` | Пароль PostgreSQL | `PostgreSQL` |
   | `ADMIN_EMAIL` | Email администратора | `admin@example.com` |
   | `ADMIN_PASSWORD` | Пароль администратора | `secret` |

   Не храните реальные пароли и секреты в Git.

4. Запустите все тесты из корня проекта:

   **Windows (PowerShell):**
   ```powershell
   .\\gradlew.bat test
   ```

   **Linux / macOS:**
   ```bash
   ./gradlew test
   ```

   Если Gradle установлен отдельно, можно использовать `gradle test`.

## Отчёты JUnit

После запуска тестов Gradle создаёт XML-отчёты, необходимые для проверки результатов:

```
lib/build/test-results/test/TEST-ru.productstar.tests.PublicRoutesTest.xml
lib/build/test-results/test/TEST-ru.productstar.tests.AuthRoutesTest.xml
lib/build/test-results/test/TEST-ru.productstar.tests.AdminRoutesTest.xml
```

HTML-отчёт доступен по пути:

```
lib/build/reports/tests/test/index.html
```

XML-файлы появляются только после фактического запуска тестов. Перед отправкой задания приложите все XML-отчёты, созданные в `lib/build/test-results/test/`.

## Структура тестовой части

```
lib/
  src/
    main/java/models/
      AuthResponse.java
    test/java/ru/productstar/tests/
      ApiTestSupport.java
      PublicRoutesTest.java
      AuthRoutesTest.java
      AdminRoutesTest.java
```

**Важно:** это интеграционные тесты. Команда Gradle не запускает PostgreSQL и Movies backend автоматически — сначала запустите и настройте эти сервисы.
