import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import lombok.Getter;
import java.net.Socket;

public class ClientMessageHandler {
    private @Getter ObjectEncoderOutputStream os;
    private @Getter ObjectDecoderInputStream is;
    private @Getter Socket socket;

    public ClientMessageHandler() {
        try {
            socket = new Socket("localhost", 8189);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
