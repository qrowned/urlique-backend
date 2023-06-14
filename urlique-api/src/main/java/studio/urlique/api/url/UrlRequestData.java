package studio.urlique.api.url;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import studio.urlique.api.database.DocumentId;

@Deprecated
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UrlRequestData {

    @DocumentId
    private String urlId;
    private int requests;
    private Timestamp lastEdited;

    public void increaseRequest() {
        requests++;
    }

}
