package hexlet.code;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.Instant;
import java.util.List;

@Entity
public class Url extends Model {

    @Id
    long id;
    String domain;
    @WhenCreated
    Instant createdAt;
    @OneToMany
    List<UrlCheck> urlChecks;

    public Url(String url) {
        this.domain = url;
    }

    public final long getId() {
        return id;
    }

    public final String getDomain() {
        return domain;
    }

    public final Instant getCreatedAt() {
        return createdAt;
    }

    public final UrlCheck getLastCheck() {
        if (urlChecks != null && !urlChecks.isEmpty()) {
            return urlChecks.get(urlChecks.size() - 1);
        }
        return null;
    }
}
