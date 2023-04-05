package hexlet.code.controllers;

import hexlet.code.Domain.Url;
import hexlet.code.Domain.UrlCheck;
import hexlet.code.Domain.query.QUrl;
import hexlet.code.Domain.query.QUrlCheck;
import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public final class UrlController {

    private static Url createUrl(String url) {
        try {
            URL newUrl = new URL(url);
            String normalizedUrl;
            String protocol = newUrl.getProtocol();
            String host = newUrl.getHost();
            int port = newUrl.getPort();
            normalizedUrl = port == -1 ? protocol + "://" + host : protocol + "://" + host + ":" + port;
            return new Url(normalizedUrl);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public static Handler addUrl = ctx -> {
        String url = ctx.formParam("url");
        Url newUrl = createUrl(url);
        if (newUrl == null) {
            ctx.sessionAttribute("flashDanger", "Некорректный URL");
            ctx.redirect("/");
            return;
        }
        boolean urlExists =
                new QUrl()
                        .name.equalTo(newUrl.getName())
                        .exists();
        if (urlExists) {
            ctx.sessionAttribute("flashInfo", "Страница уже существует");
            ctx.redirect("/urls");
            return;
        }
        newUrl.save();
        ctx.sessionAttribute("flashSuccess", "Страница успешно добавлена");
        ctx.redirect("/urls");
    };

    public static Handler showUrls = ctx -> {
        List<UrlCheck> urlChecks = new QUrlCheck()
                .id.asc()
                .findList();
        List<Url> urls = new QUrl()
                .orderBy()
                .id.asc()
                .findList();
        ctx.attribute("urls", urls);
        ctx.render("showAllUrls.html");
    };

    public static Handler showUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();
        List<UrlCheck> urlChecks = url.getUrlChecks();
        ctx.attribute("urlChecks", urlChecks);
        ctx.attribute("url", url);
        ctx.render("showUrl.html");
    };

    public static Handler checkUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (url == null) {
            ctx.sessionAttribute("flashDanger", "Некорректный URL");
            ctx.redirect("/urls/");
        }
        try {
            HttpResponse<String> response = Unirest.get(url.getName()).asString();
            int statusCode = response.getStatus();
            Document doc = Jsoup.parse(response.getBody());
            String title = doc.title();
            String h1 = doc.selectFirst("h1") != null ? doc.selectFirst("h1").text() : null;
            String description = doc.selectFirst("meta[name=description]") != null
                    ? doc.selectFirst("meta[name=description]").attr("content") : null;
            UrlCheck urlCheck = new UrlCheck(statusCode, title, h1, description, url);
            urlCheck.save();
            ctx.sessionAttribute("flashSuccess", "Страница успешно проверена");
        } catch (RuntimeException e) {
            ctx.sessionAttribute("flashDanger", "Некорректный адрес");
        }
        ctx.redirect("/urls/" + id);
    };
}