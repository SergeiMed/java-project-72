package hexlet.code;

import hexlet.code.query.QUrl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import io.javalin.Javalin;
import io.ebean.DB;
import io.ebean.Database;

public class AppTest {

    private static final int STATUS_OK = 200;
    private static final int STATUS_REDIRECT = 302;
    private static Javalin app;
    private static String baseUrl;
    private static Database database;

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        database = DB.getDefault();
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @BeforeEach
    final void beforeEach() {
        //database.script().run("/truncate.sql");
        database.script().run("/seed-test-db.sql");
    }

    @Test
    void testIndex() {
        HttpResponse<String> response = Unirest.get(baseUrl).asString();
        assertThat(response.getStatus()).isEqualTo(STATUS_OK);
        assertThat(response.getBody()).contains("Анализатор страниц");
    }
}

//    @Nested
//    class UrlTest {
//
//        @Test
//        void testCreate() {
//            String inputUrl = "https://github.com";
//            HttpResponse<String> responsePost = Unirest
//                    .post(baseUrl + "/urls")
//                    .field("url", inputUrl)
//                    .asEmpty();
//
//            assertThat(responsePost.getStatus()).isEqualTo(STATUS_REDIRECT);
//            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");
//
//            HttpResponse<String> response = Unirest
//                    .get(baseUrl + "/urls")
//                    .asString();
//            String body = response.getBody();
//
//            assertThat(response.getStatus()).isEqualTo(STATUS_OK);
//            assertThat(body).contains("github.com");
//            assertThat(body).contains("Страница успешно добавлена");
//
//            Url actualUrl = new QUrl()
//                    .name.equalTo("http://github.com")
//                    .findOne();
//
//            assertThat(actualUrl).isNotNull();
//            assertThat(actualUrl.getName()).isEqualTo("http://github.com");
//        }
//    }
//}
