package hexlet.code;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@Entity
public class Url extends Model {

    @Id
    long id;
    String domain;
    @WhenCreated
    Instant createdAt;

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
}
