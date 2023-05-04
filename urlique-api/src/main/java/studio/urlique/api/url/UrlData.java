package studio.urlique.api.url;

import com.google.cloud.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import studio.urlique.api.database.DocumentId;

import java.util.Optional;

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

    public Optional<String> getCreator() {
        return Optional.ofNullable(this.creator);
    }

    public boolean equalsCreator(@NotNull String creator) {
        return this.creator != null && this.creator.equals(creator);
    }

}
