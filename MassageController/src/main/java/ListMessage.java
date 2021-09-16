import lombok.Getter;
import lombok.ToString;

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
