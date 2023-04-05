package hexlet.code.Domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import java.time.Instant;

@Entity
public class UrlCheck extends Model {

    @Id
    private long id;
    private int statusCode;
    private String title;
    private String h1;
    @Lob
    private String description;
    @ManyToOne
    private Url url;
    @WhenCreated
    private Instant createdAt;

    public UrlCheck(int statCode, String tit, String h, String descript, Url domain) {
        this.statusCode = statCode;
        this.title = tit;
        this.h1 = h;
        this.description = descript;
        this.url = domain;
    }

    public UrlCheck() {
    }

    public final long getId() {
        return id;
    }

    public final int getStatusCode() {
        return statusCode;
    }

    public final String getTitle() {
        return title;
    }

    public final String getH1() {
        return h1;
    }

    public final String getDescription() {
        return description;
    }

    public final Url getUrl() {
        return url;
    }

    public final Instant getCreatedAt() {
        return createdAt;
    }
}
