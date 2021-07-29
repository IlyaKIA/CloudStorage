import UsersDB.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticationResponse extends AbstractCommand {
    private final String firstName;
    private final String lastName;
    private final String login;
    private final String password;

    public AuthenticationResponse(User user) {
        firstName = user.getFirstName();
        lastName = user.getLastName();
        login = user.getLogin();
        password = user.getPassword();
    }

    @Override
    public CommandType getType() {
        return CommandType.AUTH_RESPONSE;
    }
}
