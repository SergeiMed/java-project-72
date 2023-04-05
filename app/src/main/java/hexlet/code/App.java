package hexlet.code;

import hexlet.code.controllers.UrlController;
import hexlet.code.controllers.WelcomeController;
import io.javalin.Javalin;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.get;


public class App {

    private static final String DEFAULT_PORT = "5000";
    private static final String DEVELOPMENT_ENV = "development";
    private static final String PRODUCTION_ENV = "production";

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", DEFAULT_PORT);
        return Integer.parseInt(port);
    }

    private static String getMode() {
        return System.getenv().getOrDefault("APP_ENV", DEVELOPMENT_ENV);
    }

    private static boolean isProduction() {
        return getMode().equals(PRODUCTION_ENV);
    }

    private static TemplateEngine getTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");
        templateResolver.setCharacterEncoding("UTF-8");

        templateEngine.addTemplateResolver(templateResolver);
        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());

        return templateEngine;
    }

    private static void addRoutes(Javalin app) {
        app.get("/", WelcomeController.welcome);
        app.routes(() -> {
            path("/urls", () -> {
                post(UrlController.addUrl);
                post("{id}/checks", UrlController.checkUrl);
                get(UrlController.showUrls);
                get("{id}", UrlController.showUrl);
            });
        });

    }

    public static Javalin getApp() {
        Javalin app = Javalin.create(config -> {
            if (!isProduction()) {
                config.enableDevLogging();
            }
            config.enableWebjars();
            JavalinThymeleaf.configure(getTemplateEngine());
        });
        app.before(ctx -> {
            ctx.attribute("ctx", ctx);
        });
        addRoutes(app);
        return app;
    }

    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(getPort());
    }
}
