package ru.productstar.tests;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PublicRoutesTest {

    @Test
    void canGetGenres() throws Exception {
        Request request = new Request.Builder()
                .url(ApiTestSupport.API_BASE_URL + "/genres")
                .get()
                .build();

        try (Response response = ApiTestSupport.HTTP.newCall(request).execute()) {
            assertEquals(200, response.code(), "GET /genres должен вернуть HTTP 200");
            assertNotNull(response.body());

            JsonElement json = JsonParser.parseString(response.body().string());
            assertTrue(json.isJsonArray(), "Ответ должен быть JSON-массивом жанров");
            JsonArray genres = json.getAsJsonArray();
            assertFalse(genres.isEmpty(), "Список жанров не должен быть пустым");
            assertTrue(genres.get(0).getAsJsonObject().has("genre"),
                    "У жанра должно быть поле genre");
        }
    }

    @Test
    void canGetPublicMovies() throws Exception {
        Request request = new Request.Builder()
                .url(ApiTestSupport.API_BASE_URL + "/movies")
                .get()
                .build();

        try (Response response = ApiTestSupport.HTTP.newCall(request).execute()) {
            assertEquals(200, response.code(), "GET /movies должен вернуть HTTP 200");
            assertNotNull(response.body());

            JsonElement json = JsonParser.parseString(response.body().string());
            assertTrue(json.isJsonArray(), "Ответ должен быть JSON-массивом фильмов");
            JsonArray movies = json.getAsJsonArray();
            assertFalse(movies.isEmpty(), "Каталог фильмов не должен быть пустым");
            assertTrue(movies.get(0).getAsJsonObject().has("title"),
                    "У фильма должно быть поле title");
        }
    }
}
