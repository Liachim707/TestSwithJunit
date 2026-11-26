package ru.productstar.tests;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.AuthResponse;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthRoutesTest {

    @Test
    void adminCanAuthenticate() throws Exception {
        AuthResponse auth = ApiTestSupport.authenticate(
                ApiTestSupport.ADMIN_EMAIL, ApiTestSupport.ADMIN_PASSWORD);

        assertNotNull(auth.accessToken);
        assertFalse(auth.accessToken.isBlank());
        assertNotNull(auth.refreshToken);
        assertFalse(auth.refreshToken.isBlank());
    }

    @Test
    void invalidCredentialsAreRejected() throws Exception {
        String payload = ApiTestSupport.GSON.toJson(
                new ApiTestSupport.LoginPayload("wrong@example.com", "wrong-password"));
        Request request = new Request.Builder()
                .url(ApiTestSupport.API_BASE_URL + "/authenticate")
                .post(RequestBody.create(payload, ApiTestSupport.JSON))
                .build();

        try (Response response = ApiTestSupport.HTTP.newCall(request).execute()) {
            assertEquals(400, response.code(),
                    "Неверные учётные данные должны отклоняться с HTTP 400");
        }
    }

    @Test
    void adminEndpointRejectsRequestWithoutToken() throws Exception {
        Request request = new Request.Builder()
                .url(ApiTestSupport.API_BASE_URL + "/admin/movies")
                .get()
                .build();

        try (Response response = ApiTestSupport.HTTP.newCall(request).execute()) {
            assertEquals(401, response.code(),
                    "Административный endpoint должен требовать авторизацию");
        }
    }

    @Test
    void logoutExpiresRefreshCookie() throws Exception {
        Request request = new Request.Builder()
                .url(ApiTestSupport.API_BASE_URL + "/logout")
                .get()
                .build();

        try (Response response = ApiTestSupport.HTTP.newCall(request).execute()) {
            assertEquals(202, response.code(), "GET /logout должен вернуть HTTP 202");

            String setCookie = response.header("Set-Cookie");
            assertNotNull(setCookie, "Logout должен отправить истёкшую refresh-cookie");
            assertTrue(setCookie.contains("refresh_token"));
            assertTrue(setCookie.contains("Max-Age=0")
                            || setCookie.toLowerCase().contains("expires=thu, 01 jan 1970"),
                    "Refresh-cookie должна быть удалена/просрочена: " + setCookie);
        }
    }
}
