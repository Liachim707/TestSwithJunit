package ru.productstar.tests;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import models.AuthResponse;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.*;

import com.google.gson.Gson;

public class AdminRoutesTest {
    static String DB_URL = "jdbc:postgresql://localhost:5432/movies";
    static String DB_USER = "postgres";
    static String DB_PASSWORD = "postgres";
    static Connection connection;
    OkHttpClient client;

    @BeforeAll static void initDBConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            AdminRoutesTest.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @AfterAll static void closeDBConnection() {
        try {
            AdminRoutesTest.connection.close();
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @BeforeEach void initHttpClient() {
        this.client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + this.getAccessToken())
                            .build();
                    return chain.proceed(request);
                })
                .build();
    }

    String getAccessToken() {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(
                "{\"email\":\"admin@example.com\",\"password\":\"secret\"}", mediaType);
        Request request = new Request.Builder()
                .url("http://localhost:4000/v1/authenticate")
                .post(body)
                .build();
        try {
            String response = client.newCall(request).execute().body().string();
            AuthResponse authResponse = new Gson().fromJson(response, AuthResponse.class);
            return authResponse.accessToken;
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
            return "";
        }
    }

    @Test void adminCanGetMovies() {
        Request request = new Request.Builder()
                .url("http://localhost:4000/v1/admin/movies").build();
        try {
            String response = this.client.newCall(request).execute().body().string();
            System.out.println(response);
            assertTrue(response.contains("title"));
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test void patchMovie() {
        String filmTitle = "";
        String filmID = "";
        try {
            Statement statement = AdminRoutesTest.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT title, id FROM movies LIMIT 1");
            while (resultSet.next()) {
                filmTitle = resultSet.getString("title");
                filmID = resultSet.getString("id");
            }
            System.out.println("film title:" + filmTitle);
            System.out.println("film id:" + filmID);
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }

        // 1. PATCH - обновляем описание фильма
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(
                "{\"title\":\"" + filmTitle + "\",\"description\":\"new description\"}", mediaType);
        Request patchReq = new Request.Builder()
                .url("http://localhost:4000/v1/admin/movies/" + filmID)
                .patch(body)
                .build();
        try {
            this.client.newCall(patchReq).execute();
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }

        // 2. GET - читаем тот же фильм и проверяем обновление
        Request getReq = new Request.Builder()
                .url("http://localhost:4000/v1/admin/movies/" + filmID).build();
        try {
            String response = this.client.newCall(getReq).execute().body().string();
            System.out.println(response);
            assertTrue(response.contains("new description"));
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }
}