package hexlet.code.controllers;

import hexlet.code.Url;
import hexlet.code.UrlCheck;
import hexlet.code.query.QUrl;
import hexlet.code.query.QUrlCheck;
import io.ebean.PagedList;
import io.javalin.http.Handler;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public final class DomainController {

    private static final int ROWS_PER_PAGE = 10;

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
        if (!isUrl(url)) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.redirect("/");
            return;
        }
        URL urlFromRequest = new URL(url);
        String normalizedUrlFromRequest = "http://" + urlFromRequest.getAuthority();
        Url newUrl = new Url(normalizedUrlFromRequest);
        boolean urlExists =
                new QUrl()
                        .domain.equalTo(normalizedUrlFromRequest)
                        .exists();
        if (urlExists) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.redirect("/");
            return;
        }
        newUrl.save();
        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.redirect("/urls");
    };

    public static Handler showDomains = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int offset = (page - 1) * ROWS_PER_PAGE;

        List<UrlCheck> urlChecks = new QUrlCheck()
                .id.asc()
                .findList();

        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(offset)
                .setMaxRows(ROWS_PER_PAGE)
                .orderBy()
                .id.asc()
                .findPagedList();
        List<Url> urls = pagedUrls.getList();
        ctx.attribute("urlChecks", urlChecks);
        ctx.attribute("urls", urls);
        ctx.attribute("page", page);
        ctx.render("showAllUrls.html");
    };

    public static Handler showDomain = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();
        UrlCheck urlCheck = new UrlCheck(url);
        urlCheck.save();
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int offset = (page - 1) * ROWS_PER_PAGE;

        PagedList<UrlCheck> pagedUrls = new QUrlCheck()
                .setFirstRow(offset)
                .setMaxRows(ROWS_PER_PAGE)
                .orderBy()
                .url.equalTo(url)
                .id.asc()
                .findPagedList();
        List<UrlCheck> urlChecks = pagedUrls.getList();

        ctx.attribute("urlChecks", urlChecks);
        ctx.attribute("url", url);
        ctx.render("showUrl.html");
    };

//    public static Handler checkDomain = ctx -> {
//        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
//        Url url = new QUrl()
//                .id.equalTo(id)
//                .findOne();
//        UrlCheck urlCheck = new UrlCheck(url);
//        urlCheck.save();
//        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
//        int offset = (page - 1) * ROWS_PER_PAGE;
//
//        PagedList<UrlCheck> pagedUrls = new QUrlCheck()
//                .setFirstRow(offset)
//                .setMaxRows(ROWS_PER_PAGE)
//                .orderBy()
//                .url.equalTo(url)
//                .id.asc()
//                .findPagedList();
//        List<UrlCheck> urlChecks = pagedUrls.getList();
//
//        ctx.attribute("urlChecks", urlChecks);
//        ctx.attribute("url", url);
//        ctx.render("showUrl.html");
//        ctx.redirect("/urls/" + id);
//    };
}
