package ru.productstar.tests;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AdminRoutesTest {

    @Test
    void adminCanGetMovies() throws Exception {
        String token = ApiTestSupport.adminToken();
        Request request = ApiTestSupport.adminRequest(
                ApiTestSupport.API_BASE_URL + "/admin/movies", token).get().build();

        try (Response response = ApiTestSupport.HTTP.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertNotNull(response.body());
            JsonObject[] movies = ApiTestSupport.GSON.fromJson(
                    response.body().string(), JsonObject[].class);
            assertNotNull(movies);
            assertTrue(movies.length > 0, "Административный каталог не должен быть пустым");
            assertTrue(movies[0].has("title"));
        }
    }

    @Test
    void adminCanGetMovieById() throws Exception {
        String token = ApiTestSupport.adminToken();
        int movieId = findExistingMovieId();

        Request request = ApiTestSupport.adminRequest(
                ApiTestSupport.API_BASE_URL + "/admin/movies/" + movieId, token).get().build();

        try (Response response = ApiTestSupport.HTTP.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertNotNull(response.body());
            JsonObject payload = JsonParser.parseString(response.body().string()).getAsJsonObject();
            assertTrue(payload.has("movie"), "Ответ должен содержать объект movie");
            assertEquals(movieId, payload.getAsJsonObject("movie").get("id").getAsInt());
        }
    }

    @Test
    void patchMovie() throws Exception {
        String token = ApiTestSupport.adminToken();
        int movieId = findExistingMovieId();

        String payload = """
                {
                  "id": %d,
                  "title": "API test updated movie",
                  "release_date": "1986-03-07T00:00:00Z",
                  "runtime": 116,
                  "mpaa_rating": "R",
                  "description": "updated by JUnit API test",
                  "image": ""
                }
                """.formatted(movieId);

        Request request = ApiTestSupport.adminRequest(
                ApiTestSupport.API_BASE_URL + "/admin/movies/" + movieId, token)
                .patch(RequestBody.create(payload, ApiTestSupport.JSON)).build();

        try (Response response = ApiTestSupport.HTTP.newCall(request).execute()) {
            assertEquals(200, response.code());
        }

        Request getRequest = ApiTestSupport.adminRequest(
                ApiTestSupport.API_BASE_URL + "/admin/movies/" + movieId, token).get().build();

        try (Response response = ApiTestSupport.HTTP.newCall(getRequest).execute()) {
            assertEquals(200, response.code());
            JsonObject result = JsonParser.parseString(response.body().string()).getAsJsonObject();
            assertEquals("updated by JUnit API test",
                    result.getAsJsonObject("movie").get("description").getAsString());
        }
    }

    @Test
    void deleteMovieCreatedInDatabaseDuringArrange() throws Exception {
        String token = ApiTestSupport.adminToken();
        int createdMovieId;

        // Arrange: the test record is inserted directly into PostgreSQL.
        try (Connection connection = ApiTestSupport.openDatabase();
             PreparedStatement insert = connection.prepareStatement("""
                     INSERT INTO movies
                         (title, description, release_date, runtime, mpaa_rating,
                          created_at, updated_at, image)
                     VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?)
                     RETURNING id
                     """)) {
            insert.setString(1, "JUnit delete test " + Instant.now());
            insert.setString(2, "Created directly in PostgreSQL for the delete test");
            insert.setTimestamp(3,
                    java.sql.Timestamp.from(Instant.parse("2000-01-01T00:00:00Z")));
            insert.setInt(4, 90);
            insert.setString(5, "PG");
            insert.setString(6, "");
            try (ResultSet result = insert.executeQuery()) {
                assertTrue(result.next(), "PostgreSQL должен вернуть ID созданного фильма");
                createdMovieId = result.getInt("id");
            }
        }

        // Act: delete the newly inserted record through the admin API.
        Request deleteRequest = ApiTestSupport.adminRequest(
                ApiTestSupport.API_BASE_URL + "/admin/movies/" + createdMovieId, token)
                .delete().build();

        try (Response response = ApiTestSupport.HTTP.newCall(deleteRequest).execute()) {
            assertEquals(200, response.code());
        } finally {
            // Ensure test data is removed even if the API call fails.
            try (Connection connection = ApiTestSupport.openDatabase();
                 PreparedStatement cleanup = connection.prepareStatement(
                         "DELETE FROM movies WHERE id = ?")) {
                cleanup.setInt(1, createdMovieId);
                cleanup.executeUpdate();
            }
        }

        // Assert: the row must no longer exist.
        try (Connection connection = ApiTestSupport.openDatabase();
             PreparedStatement select = connection.prepareStatement(
                     "SELECT COUNT(*) FROM movies WHERE id = ?")) {
            select.setInt(1, createdMovieId);
            try (ResultSet result = select.executeQuery()) {
                assertTrue(result.next());
                assertEquals(0, result.getInt(1));
            }
        }
    }

    private int findExistingMovieId() throws Exception {
        try (Connection connection = ApiTestSupport.openDatabase();
             Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "SELECT id FROM movies ORDER BY id LIMIT 1")) {
            assertTrue(result.next(), "В БД должен существовать хотя бы один фильм");
            return result.getInt("id");
        }
    }
}
