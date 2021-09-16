import UsersDB.DB_Const;
import UsersDB.DB_Handler;
import UsersDB.User;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<AbstractCommand> {
    private Path currentPath;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractCommand command) {
        log.debug("received: {}", command);
        switch (command.getType()) {
            case FILE_MESSAGE:
                FileMessage message = (FileMessage) command;
                try (FileOutputStream fos = new FileOutputStream(currentPath.resolve(message.getName()).toString())) {
                    fos.write(message.getData());
                    ctx.writeAndFlush(new ListResponse(currentPath));
                    ctx.writeAndFlush(new Message("File sending successful"));
                } catch (Exception e) {
                    ctx.writeAndFlush(new Message("Sending error in block: FILE_MESSAGE"));
                }
                break;
            case LIST_MESSAGE:
                try {
                    ctx.writeAndFlush(new Message("Server file list refreshing"));
                    ctx.writeAndFlush(new ListResponse(currentPath));
                    ctx.writeAndFlush(new PathUpResponse(currentPath.toString()));
                } catch (IOException e) {
                    ctx.writeAndFlush(new Message("Sending error in block: LIST_MESSAGE"));
                }
                ctx.writeAndFlush(new Message("Server file list refreshed"));
                break;
            case FILE_REQUEST:
                FileRequest requestedFileName = (FileRequest) command;
                try {
                    ctx.writeAndFlush(new FileMessage(currentPath.resolve(requestedFileName.getName())));
                } catch (Exception e) {
                    ctx.writeAndFlush(new Message("Sending error in block: FILE_REQUEST"));
                }
                break;
            case PATCH_UP:
                try {
                    if(currentPath.getParent() != null) {
                        currentPath = currentPath.getParent();
                        ctx.writeAndFlush(new PathUpResponse(currentPath.toString()));
                        ctx.writeAndFlush(new ListResponse(currentPath));
                    }
                } catch (Exception e) {
                    ctx.writeAndFlush(new Message("Sending error in block: PATCH_UP"));
                }
                break;
            case PATH_IN_REQUEST:
                try {
                    PathInRequest request = (PathInRequest) command;
                    Path newPath = currentPath.resolve(request.getDir());
                    if (Files.isDirectory(newPath)) {
                        currentPath = newPath;
                        ctx.writeAndFlush(new PathUpResponse(currentPath.toString()));
                        ctx.writeAndFlush(new ListResponse(currentPath));
                    }
                } catch (Exception e) {
                    ctx.writeAndFlush(new Message("Sending error in block: PATH_IN_REQUEST"));
                }
                break;
            case DELETE_REQUEST:
                DeleteRequest request = (DeleteRequest) command;
                Path delPath = currentPath.resolve(request.fileName);
                boolean isDeleted = delPath.toFile().delete();
                try {
                    ctx.writeAndFlush(new ListResponse(currentPath));
                } catch (IOException e) {
                    ctx.writeAndFlush(new Message("Sending error in block: DELETE_REQUEST"));
                }
                if (isDeleted) {
                    ctx.writeAndFlush(new Message("File " + request.fileName + " deleted successful"));
                } else {
                    ctx.writeAndFlush(new Message("File " + request.fileName + " deleting error"));
                }
                break;
            case AUTH_REQUEST:
                AuthenticationRequest authRequest = (AuthenticationRequest) command;
                User user = new User();
                user.setLogin(authRequest.getLoginText());
                user.setPassword(authRequest.getPasswordText());
                ResultSet resSet = new DB_Handler().getUser(user);
                try {
                        resSet.next();
                        user.setFirstName(resSet.getString(DB_Const.USER_FIRSTNAME));
                        user.setLastName(resSet.getString(DB_Const.USER_LASTNAME));
                        currentPath = Paths.get(user.getLogin()); //TODO: I can't set structure 'server/User', where User directory is ROOT..
                        if (!Files.exists(currentPath)) {
                            Files.createDirectory(currentPath);
                        }
                        ctx.writeAndFlush(new AuthenticationResponse(user));
                } catch (SQLException throwables) {
                    log.error("Error: {}", throwables.getClass());
                    ctx.writeAndFlush(new Message("Authentication failed"));
                } catch (IOException e) {
                    log.error("Error: {}", e.getClass());
                }
                break;
            case REG_REQUEST:
                RegistrationRequest regMSG = (RegistrationRequest) command;
                User newUser = new User(regMSG.getFirstNameText(), regMSG.getLastNameText(), regMSG.getLoginText(), regMSG.getPasswordText());
                try {
                    new DB_Handler().signUpUser(newUser.getLogin(),newUser.getPassword(), newUser.getFirstName(), newUser.getLastName());
                    ctx.writeAndFlush(new Message("Registration successful"));
                } catch (SQLException throwables) {
                    log.debug("message: {}", "User already exists or data base error");
                    ctx.writeAndFlush(new Message("User already exists or data base error"));
                }
                break;
            case MK_DIR_REQUEST:
                MkDirRequest mkDirRequest = (MkDirRequest) command;
                currentPath = currentPath.resolve(mkDirRequest.getDirName());
                currentPath.toFile().mkdir();
                currentPath = currentPath.getParent();
                try {
                    ctx.writeAndFlush(new ListResponse(currentPath));
                    ctx.writeAndFlush(new Message("A new directory has been added"));
                } catch (IOException e) {
                    log.debug("message: {}", "Error making directory to server");
                    ctx.writeAndFlush(new Message("Error making directory to server"));
                }
                break;
        }
    }
}
