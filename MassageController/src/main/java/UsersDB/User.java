package UsersDB;

import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
public class User {
    private @Getter @Setter String firstName;
    private @Getter @Setter String lastName;
    private @Getter @Setter String login;
    private @Getter @Setter String password;
}

