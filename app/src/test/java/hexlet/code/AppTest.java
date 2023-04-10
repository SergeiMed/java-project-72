package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.Transaction;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;


import static org.assertj.core.api.Assertions.assertThat;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import io.javalin.Javalin;
import io.ebean.DB;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class AppTest {
    private static Javalin app;
    private static String baseUrl;
    private static Transaction transaction;
    private static MockWebServer mockWebServer;
    private static String normalizedMockWebServerUrl;
    private static Url existingUrl;

    @BeforeAll
    public static void beforeAll() throws Exception {
        mockWebServer = new MockWebServer();
        String testServerHtml = Files.readString(Paths.get("src/test/resources/mockServerHtml.html"));
        mockWebServer.enqueue(new MockResponse().setBody(testServerHtml));
        mockWebServer.start();
        String mockWebServerUrl = mockWebServer.url("/").toString();
        normalizedMockWebServerUrl = mockWebServerUrl.substring(0, mockWebServerUrl.length() - 1);

        existingUrl = new QUrl()
                .id.equalTo(1)
                .findOne();


        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
    }

    @AfterAll
    public static void afterAll() throws IOException {
        mockWebServer.shutdown();
        app.stop();
    }

    @BeforeEach
    void beforeEach() {
        transaction = DB.beginTransaction();
    }

    @AfterEach
    void afterEach() {
        transaction.rollback();
    }

    @Nested
    class RootTest {

        @Test
        void testIndex() {
            HttpResponse<String> responseGet = Unirest.get(baseUrl).asString();
            assertThat(responseGet.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
            assertThat(responseGet.getBody()).contains("Анализатор страниц");
        }
    }
    @Nested
    class UrlTest {

        @Test
        void testShowUrl() {
            String id = Long.toString(existingUrl.getId());
            HttpResponse<String> responseGet = Unirest
                    .get(baseUrl + "/urls/" + id)
                    .asString();
            String body = responseGet.getBody();
            assertThat(responseGet.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
            assertThat(body).contains(existingUrl.getName());
        }

        @Test
        void testShowUrls() {
            HttpResponse<String> responseGet = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = responseGet.getBody();
            assertThat(responseGet.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
            assertThat(body).contains(existingUrl.getName());
        }
        @Test
        void testCreate() {
            String url = "https://www.youtube.com";
            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", url)
                    .asString();

            Url actualUrl = new QUrl()
                    .name.equalTo(url)
                    .findOne();

            assertThat(responsePost.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
            assertThat(body).contains(url);
            assertThat(body).contains("Страница успешно добавлена");
            assertThat(actualUrl).isNotNull();
            assertThat(actualUrl.getName()).isEqualTo(url);
        }

        @Test
        void testExistingUrl() {
            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", existingUrl.getName())
                    .asString();

            assertThat(responsePost.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

            HttpResponse<String> response = Unirest
                    .get(baseUrl)
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
            assertThat(body).contains("Страница уже существует");
        }

        @Test
        void testIncorrectUrl() {
            String incorrectUrl = "incorrect url";
            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", incorrectUrl)
                    .asString();

            assertThat(responsePost.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/");

            HttpResponse<String> response = Unirest
                    .get(baseUrl)
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
            assertThat(body).contains("Некорректный URL");

        }

        @Test
        void testCheckUrl() {
            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", normalizedMockWebServerUrl)
                    .asString();

            Url createdUrl = new QUrl()
                    .name.equalTo(normalizedMockWebServerUrl)
                    .findOne();
            String id = Long.toString(createdUrl.getId());

            HttpResponse<String> responsePostCheck = Unirest
                    .post(baseUrl + "/urls/" + id + "/checks")
                    .asString();

            UrlCheck latestCheck = new QUrlCheck()
                    .url.equalTo(createdUrl)
                    .orderBy()
                    .id
                    .desc()
                    .findOne();

            HttpResponse<String> responseGet = Unirest
                    .get(baseUrl + "/urls/" + id)
                    .asString();

            assertThat(createdUrl).isNotNull();
            assertThat(responseGet.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
            assertThat(responseGet.getBody()).contains(normalizedMockWebServerUrl);
            assertThat(responseGet.getBody()).contains("Страница успешно проверена");
            assertThat(latestCheck.getDescription()).contains("Test description");
            assertThat(latestCheck.getH1()).isEqualTo("Test H1");
        }

        @Test
        void testCheckIncorrectUrl() {
            String url = "https://www.youtubeyouu.com";
            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", url)
                    .asString();

            Url createdUrl = new QUrl()
                    .name.equalTo(url)
                    .findOne();
            String id = Long.toString(createdUrl.getId());

            HttpResponse<String> responsePostCheck = Unirest
                    .post(baseUrl + "/urls/" + id + "/checks")
                    .asString();

            HttpResponse<String> responseGet = Unirest
                    .get(baseUrl + "/urls/" + id)
                    .asString();

            assertThat(responsePostCheck.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
            assertThat(responsePostCheck.getHeaders().getFirst("Location")).isEqualTo("/urls/" + id);
            assertThat(responsePost.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");
            assertThat(createdUrl).isNotNull();
            assertThat(responseGet.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
            assertThat(responseGet.getBody()).contains(url);
            assertThat(responseGet.getBody()).contains("Некорректный адрес");
        }
    }
}

