import javafx.scene.control.Button;

import java.io.*;
import java.net.Socket;

import static javafx.scene.paint.Color.GREEN;

public class NetworkProcessor  {
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private byte[] buffer;

    protected void Connect(Socket socket, Button connectButt) throws IOException {
        this.socket = socket;
        is = socket.getInputStream();
        os = socket.getOutputStream();
        buffer = new byte[1024];
        new Thread(new Runnable() {
            @Override
            public void run() {
                StringBuilder builder = new StringBuilder();
                while (true){
                    try {
                        int read = is.read(buffer);
                        for (int i = 0; i < read; i++) {
                            if (buffer[i] != '\r') {
                                builder.append((char) buffer[i]);
                            } else {
                                String message = builder.toString().trim();
                                System.out.println(message);
                                if (message.equals("/Hello")) {
//                                    isConnected = true;
                                    connectButt.setTextFill(GREEN);
                                }
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Disconnected");
                        break;
                    }
                }
            }
        }).start();
    }

    public void SandFile(String filePath) {
        try (InputStream inFileStream = new BufferedInputStream(new FileInputStream(filePath))){
            int fileByte;
            while ((fileByte = inFileStream.read()) != -1 ) {
//                System.out.print((char)fileByte);
                    os.write(fileByte);
                }
            os.write('\r');
            os.flush();
        } catch (FileNotFoundException e) {
            System.out.println("Reading file error");
        } catch (IOException e) {
            System.out.println("Sending error");
        }
    }
}
