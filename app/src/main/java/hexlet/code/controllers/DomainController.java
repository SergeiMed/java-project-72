package hexlet.code.controllers;

import hexlet.code.Url;
import hexlet.code.UrlCheck;
import hexlet.code.query.QUrl;
import hexlet.code.query.QUrlCheck;
import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public final class DomainController {

    public static boolean isUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (URISyntaxException | MalformedURLException e) {
            return false;
        }
    }

    public static Handler addDomain = ctx -> {
        String url = ctx.formParam("url");
        if (!isUrl(url) && !url.equals("")) {
            ctx.sessionAttribute("flashDanger", "Некорректный URL");
            ctx.redirect("/");
            return;
        }
        if (!url.equals("")) {
            URL urlFromRequest = new URL(url);
            String normalizedUrlFromRequest = "http://" + urlFromRequest.getAuthority();
            Url newUrl = new Url(normalizedUrlFromRequest);
            boolean urlExists =
                    new QUrl()
                            .name.equalTo(normalizedUrlFromRequest)
                            .exists();
            if (urlExists) {
                ctx.sessionAttribute("flashInfo", "Страница уже существует");
                ctx.redirect("/urls");
                return;
            }
            newUrl.save();
            ctx.sessionAttribute("flashSuccess", "Страница успешно добавлена");
        }
        ctx.redirect("/urls");
    };

    public static Handler showDomains = ctx -> {
        List<UrlCheck> urlChecks = new QUrlCheck()
                .id.asc()
                .findList();
        List<Url> urls = new QUrl()
                .orderBy()
                .id.asc()
                .findList();
        ctx.attribute("urlChecks", urlChecks);
        ctx.attribute("urls", urls);
        ctx.render("showAllUrls.html");
    };

    public static Handler showDomain = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();
        List<UrlCheck> urlChecks = new QUrlCheck()
                .orderBy()
                .url.equalTo(url)
                .id.desc()
                .findList();
        ctx.attribute("urlChecks", urlChecks);
        ctx.attribute("url", url);
        ctx.render("showUrl.html");
    };

    public static Handler checkDomain = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();
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
        } catch (RuntimeException e) {
            ctx.sessionAttribute("flashDanger", "Некорректный адрес");
        }
        ctx.redirect("/urls/" + id);
    };
}
