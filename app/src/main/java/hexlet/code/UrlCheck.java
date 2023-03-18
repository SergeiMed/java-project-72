package hexlet.code;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.*;
import java.time.Instant;
@Entity
public class UrlCheck extends Model {

    @Id
    long id;
    int statusCode;
    String title;
    String h1;
    @Lob
    String description;
    @ManyToOne
    Url url;
    @WhenCreated
    Instant createdAt;

    public UrlCheck(Url url) {
        this.url = url;
    }

    public UrlCheck() {
    }

    public long getId() {
        return id;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getTitle() {
        return title;
    }

    public String getH1() {
        return h1;
    }

    public String getDescription() {
        return description;
    }

    public Url getUrl() {
        return url;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
