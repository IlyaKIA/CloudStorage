import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
public class Message extends AbstractCommand {
    private String content;

    public String toString() {
        return this.content;
    }
    public CommandType getType() {
        return CommandType.SIMPLE_MESSAGE;
    }
}
