package studio.urlique.api.url;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    public UrlData(String id,
                   String url,
                   String creator) {
        this.id = id;
        this.url = url;
        this.creator = creator;
        this.createdAt = Timestamp.now();
    }
}
