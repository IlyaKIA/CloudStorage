import lombok.Getter;
import lombok.ToString;

import java.nio.file.Path;

@ToString
@Getter
public class FileRequest extends AbstractCommand {

    private final String name;

    public FileRequest(Path path) {
        name = path.getFileName().toString();
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_REQUEST;
    }
}
