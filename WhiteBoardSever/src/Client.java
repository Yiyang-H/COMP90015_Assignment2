import java.io.*;
import java.net.Socket;
import java.util.Random;

/**
 * A client object represents a client in the client application
 */
public class Client {
    private Socket socket;
    private String clientName;
    private String suffix;

    private DataInputStream input;
    private DataOutputStream output;

    public Client(Socket socket, String clientName) {
        this.socket = socket;
        this.clientName = clientName;
        // Randomly generate a suffix to append to the name, this is to prevent users with the same name,
        // It's very unlikely now, but still possible that clients have the same name. Can maintain a set
        // to prevent this to happen.
        this.suffix = String.format("#%04d", (new Random()).nextInt(10000));
        try{
            this.input = new DataInputStream(socket.getInputStream());
            this.output = new DataOutputStream(socket.getOutputStream());
        }catch (IOException e) {
            e.printStackTrace();
        }


    }

    public String getName() {
        return clientName+suffix;
    }

    public Socket getSocket() {
        return socket;
    }

    public DataInputStream getInput() {
        return input;
    }

    public DataOutputStream getOutput() {
        return output;
    }
}
