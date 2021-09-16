import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ToString
@Getter
public class ListRequest extends AbstractCommand {

    private  List<String> fileNameList;

    public ListRequest(Path path) throws IOException {
        fileNameList = Arrays.asList(Objects.requireNonNull(path.toFile().list()));
    }

    @Override
    public CommandType getType() {
        return CommandType.LIST_REQUEST;
    }
}
