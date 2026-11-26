package ru.productstar.tests;

import com.google.gson.Gson;
import models.AuthResponse;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

final class ApiTestSupport {
    static final String API_BASE_URL =
            System.getenv().getOrDefault("API_BASE_URL", "http://localhost:4000/v1");
    static final String ADMIN_EMAIL =
            System.getenv().getOrDefault("ADMIN_EMAIL", "admin@example.com");
    static final String ADMIN_PASSWORD =
            System.getenv().getOrDefault("ADMIN_PASSWORD", "secret");

    static final String DB_URL =
            System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5433/test_database");
    static final String DB_USER =
            System.getenv().getOrDefault("DB_USER", "postgres");
    static final String DB_PASSWORD =
            System.getenv().getOrDefault("DB_PASSWORD", "PostgreSQL");

    static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    static final Gson GSON = new Gson();
    static final OkHttpClient HTTP = new OkHttpClient();

    private ApiTestSupport() {
    }

    static AuthResponse authenticate(String email, String password) throws IOException {
        String payload = GSON.toJson(new LoginPayload(email, password));
        Request request = new Request.Builder()
                .url(API_BASE_URL + "/authenticate")
                .post(RequestBody.create(payload, JSON))
                .build();

        try (Response response = HTTP.newCall(request).execute()) {
            String body = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                throw new AssertionError("Authentication failed. HTTP "
                        + response.code() + ", response: " + body);
            }
            AuthResponse auth = GSON.fromJson(body, AuthResponse.class);
            if (auth == null || auth.accessToken == null || auth.accessToken.isBlank()) {
                throw new AssertionError("The authentication response has no access_token: " + body);
            }
            return auth;
        }
    }

    static String adminToken() throws IOException {
        return authenticate(ADMIN_EMAIL, ADMIN_PASSWORD).accessToken;
    }

    static Request.Builder adminRequest(String url, String token) {
        return new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token);
    }

    static Connection openDatabase() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    static final class LoginPayload {
        final String email;
        final String password;

        LoginPayload(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }
}
