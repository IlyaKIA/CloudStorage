import lombok.Getter;
import lombok.ToString;

import java.nio.file.Path;

@ToString
@Getter
public class PathInRequest extends AbstractCommand {

    private final String dir;

    public PathInRequest(String dir) {
        this.dir = dir;
    }

    @Override
    public CommandType getType() {
        return CommandType.PATH_IN_REQUEST;
    }
}
