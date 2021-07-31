package server;

import com.sun.xml.internal.bind.v2.TODO;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Handler implements Runnable {
    private String dir = "C:\\Server\\KuchaevIA"; //TODO
    private final Socket socket;
    private final InputStream is;
    private final OutputStream os;
    private final byte[] buffer;
    private BufferedOutputStream fileOut;



    public Handler(Socket socket) throws IOException {
        this.socket = socket;
        is = socket.getInputStream();
        os = socket.getOutputStream();
        buffer = new byte[1024];
        fileOut = new BufferedOutputStream(new FileOutputStream(dir + "\\" + "demo.txt", false));
    }

    @Override
    public void run() {
        try {
            os.write("/Hello\r".getBytes(StandardCharsets.UTF_8));
            os.flush();
            while(true){
                int read = is.read(buffer);
                for (int i = 0; i < read; i++) {
                    if (buffer[i] != '\r') {
                        System.out.print(buffer[i]);
                        fileOut.write(buffer[i]);
                    } else {
                        fileOut.flush();
                        System.out.println("file saving is end");
//                        os.flush();
//                        os.write("/file saving in server".getBytes(StandardCharsets.UTF_8));
//                        os.flush();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
