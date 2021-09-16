import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RegistrationRequest extends AbstractCommand {
    private final String loginText;
    private final String passwordText;
    private final String firstNameText;
    private final String lastNameText;

    @Override
    public CommandType getType() {
        return CommandType.REG_REQUEST;
    }
}
