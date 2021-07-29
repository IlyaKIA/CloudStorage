import lombok.Getter;
import lombok.ToString;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@ToString
@Getter
public class ListResponse extends AbstractCommand {

    private final List<String> fileNameList;
    private boolean root;

    public ListResponse(Path path) throws IOException {
        if (path.getParent() == null) {
            root = true;
        }
        fileNameList = Arrays.asList(Objects.requireNonNull(path.toFile().list()));
    }

    @Override
    public CommandType getType() {
        return CommandType.LIST_RESPONSE;
    }
}
