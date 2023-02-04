package hexlet.code.controllers;

import io.javalin.http.Handler;

public class WelcomeController {

    public static Handler welcome = ctx -> {
        ctx.render("index.html");
    };
}
