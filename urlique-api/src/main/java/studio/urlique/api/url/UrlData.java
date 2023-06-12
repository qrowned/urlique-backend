package studio.urlique.api.url;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import studio.urlique.api.database.DocumentId;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UrlData {

    @DocumentId
    private String id;
    private String url;
    private Timestamp createdAt;
    private String creator;
    private long requests = 0;

    public UrlData(String id,
                   String url,
                   String creator) {
        this.id = id;
        this.url = url;
        this.creator = creator;
        this.createdAt = Timestamp.now();
    }

    public boolean equalsCreator(@NotNull String creator) {
        return this.creator != null && this.creator.equals(creator);
    }

    public void increaseRequest() {
        requests++;
    }

}
