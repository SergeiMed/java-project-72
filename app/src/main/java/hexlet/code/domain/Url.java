package hexlet.code.domain;

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
    String name;
    @WhenCreated
    Instant createdAt;
    @OneToMany
    List<UrlCheck> urlChecks;

    public Url(String domain) {
        this.name = domain;
    }

    public final long getId() {
        return id;
    }

    public final String getName() {
        return name;
    }

    public final Instant getCreatedAt() {
        return createdAt;
    }

    public final List<UrlCheck> getUrlChecks() {
        return urlChecks;
    }

    public final UrlCheck getLastCheck() {
        if (urlChecks != null && !urlChecks.isEmpty()) {
            return urlChecks.get(urlChecks.size() - 1);
        }
        return null;
    }
}
