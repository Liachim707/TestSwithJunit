# TestSwithJunit

## Описание проекта

**TestSwithJunit** — учебный Java-проект с автоматизированными интеграционными тестами REST API приложения [Movies](https://github.com/tirzasrwn/movies). Тесты проверяют работу публичных и административных HTTP-эндпоинтов, а также сценарии авторизации и управления фильмами.

## Что делает проект

Тесты сгруппированы по назначению:

- **PublicRoutesTest** проверяет получение списка жанров (`GET /v1/genres`) и публичного списка фильмов (`GET /v1/movies`).
- **AuthRoutesTest** проверяет успешную и неуспешную авторизацию, запрет доступа к административному API без токена и выход из системы (`GET /v1/logout`).
- **AdminRoutesTest** проверяет получение административного списка фильмов, получение фильма по ID, обновление и удаление фильма.

В тесте удаления фильм сначала создаётся напрямую в PostgreSQL в этапе Arrange, затем удаляется HTTP-запросом к административному API. После этого тест проверяет, что запись действительно отсутствует в базе данных.

## Что необходимо для запуска

Перед запуском тестов подготовьте:

1. **JDK 17 или новее.**
2. **PostgreSQL** с базой данных, используемой приложением Movies.
3. Запущенный **Movies backend**, доступный по адресу `http://localhost:4000`.
4. Gradle Wrapper, включённый в репозиторий.

Тесты не запускают backend или PostgreSQL самостоятельно — эти сервисы должны быть запущены отдельно.

## Как запустить проект

### 1. Клонировать репозиторий

```bash
git clone https://github.com/Liachim707/TestSwithJunit.git
cd TestSwithJunit
```

### 2. Проверить настройки подключения

По умолчанию тесты используют следующие параметры:

| Параметр | Значение по умолчанию |
|---|---|
| Адрес API | `http://localhost:4000/v1` |
| JDBC URL | `jdbc:postgresql://localhost:5433/test_database` |
| Пользователь PostgreSQL | `postgres` |
| Пароль PostgreSQL | `PostgreSQL` |
| Email администратора | `admin@example.com` |
| Пароль администратора | `secret` |

Если ваши настройки отличаются, задайте переменные окружения:

- `API_BASE_URL` — базовый адрес API, включая `/v1`;
- `DB_URL` — JDBC URL базы данных;
- `DB_USER` и `DB_PASSWORD` — учётные данные PostgreSQL;
- `ADMIN_EMAIL` и `ADMIN_PASSWORD` — учётные данные администратора API.

Не публикуйте реальные пароли и другие секреты в репозитории.

### 3. Запустить тесты

Выполните команду из корневой папки проекта.

**Windows (PowerShell):**

```powershell
.\gradlew.bat test
```

**Linux / macOS:**

```bash
./gradlew test
```

Если Gradle установлен в системе, также можно выполнить:

```bash
gradle test
```

Gradle соберёт проект и запустит тесты из модуля `lib`.

## Где посмотреть результаты

После выполнения тестов Gradle сформирует JUnit XML-отчёты:

```
lib/build/test-results/test/
```

В этой папке должны появиться отчёты для тестовых классов, например:

- `TEST-ru.productstar.tests.PublicRoutesTest.xml`
- `TEST-ru.productstar.tests.AuthRoutesTest.xml`
- `TEST-ru.productstar.tests.AdminRoutesTest.xml`

HTML-отчёт находится здесь:

```
lib/build/reports/tests/test/index.html
```

Отчёты создаются после фактического запуска тестов. Для сдачи задания приложите XML-файлы, созданные в `lib/build/test-results/test/`.
