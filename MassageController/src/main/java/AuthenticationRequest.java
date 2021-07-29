import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticationRequest extends AbstractCommand {
    private final String loginText;
    private final String passwordText;

    @Override
    public CommandType getType() {
        return CommandType.AUTH_REQUEST;
    }
}
