import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<AbstractCommand> {
    private Path currentPath;

    public MessageHandler() {
        currentPath = Paths.get("server_dir");
    }

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
        }
    }
}
