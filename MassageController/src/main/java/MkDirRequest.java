import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MkDirRequest extends AbstractCommand {
    private final String dirName;

    @Override
    public CommandType getType() {
        return CommandType.MK_DIR_REQUEST;
    }
}
