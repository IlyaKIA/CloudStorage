import jdk.nashorn.internal.objects.annotations.Constructor;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeleteRequest extends AbstractCommand{
    String fileName;

    @Override
    public CommandType getType() {
        return CommandType.DELETE_REQUEST;
    }
}
