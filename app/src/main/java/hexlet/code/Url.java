package hexlet.code;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Id;
import java.time.Instant;
import java.util.Date;

public class Url extends Model {

    @Id
    long id;
    String name;
    @WhenCreated
    Instant createdAt;

    public Url(long id, String name, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public Url(String dbName, long id, String name, Instant createdAt) {
        super(dbName);
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
