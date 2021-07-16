import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.application.Platform;
import javafx.scene.control.ListView;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<AbstractCommand> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractCommand command) {
        log.debug("received: {}", command);
        switch (command.getType()) {
            case FILE_MESSAGE:
                FileMessage message = (FileMessage) command;
                try (FileOutputStream fos = new FileOutputStream("server_dir/" + message.getName())) {
                    fos.write(message.getData());
                    ctx.writeAndFlush(new Message("File sending successful"));
                } catch (Exception e) {
                    ctx.writeAndFlush(new Message("Sending error"));
                }
                break;
            case LIST_MESSAGE:
                try {
                    ctx.writeAndFlush(new ListRequest(Paths.get("server_dir/")));
                } catch (IOException e) {
                    ctx.writeAndFlush(new Message("Sending error"));
                }
                ctx.writeAndFlush(new Message("Server file list refreshed"));
                break;
//            case LIST_REQUEST:
//                    ListRequest listRequest = (ListRequest) command;
//                List<String> serverFileList = listRequest.getFileNameList();
//                break;
            case FILE_REQUEST:
                FileRequest requestedFileName = (FileRequest) command;
                try {
                    ctx.writeAndFlush(new FileMessage(Paths.get("server_dir/", requestedFileName.getName())));
                } catch (Exception e) {
                    ctx.writeAndFlush(new Message("Sending error"));
                }
                break;
        }
    }
}
