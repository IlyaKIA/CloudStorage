import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@ToString
@Getter
public class ListMessage extends AbstractCommand {

    public ListMessage () {

    }

    @Override
    public CommandType getType() {
        return CommandType.LIST_MESSAGE;
    }
}
