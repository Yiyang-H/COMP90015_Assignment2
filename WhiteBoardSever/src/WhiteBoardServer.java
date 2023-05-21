import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The server that serves request
 */
public class WhiteBoardServer {

    public static ConcurrentHashMap<Integer,WhiteBoardSession> sessions = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        if(args.length < 1) {
            System.out.println("Specify the port the server is listening on!");
            System.exit(1);
        }

        try {
            int serverPort = Integer.parseInt(args[0]);
            ServerSocket server = new ServerSocket(serverPort);
            while(true) {
                Socket clientSocket = server.accept();
                Thread t = new Thread(() -> handleRequest(clientSocket));
                t.start();
            }
        } catch (NumberFormatException e) {
            System.out.println("Give a valid port number!!");
            System.exit(1);

        }catch (Exception e) {
            System.out.println("Give a valid port number!!");
            System.exit(1);
        }

    }

    public static void handleRequest(Socket client) {
        System.out.println("request received");
        try {
            DataInputStream input = new DataInputStream(client.getInputStream());
            DataOutputStream output = new DataOutputStream(client.getOutputStream());
            String command = input.readUTF();
            System.out.println("command" + command);
            JSONParser parser = new JSONParser();
            JSONObject object = (JSONObject) parser.parse(command);
            String operation = (String) object.get("operation");
            switch(operation) {
                case "Rooms":
                    // return a list of rooms
                    JSONArray rooms = new JSONArray();
                    for(WhiteBoardSession session : sessions.values()) {
                        rooms.add(session.toJSONObject());
                    }
                    object.put("rooms", rooms);
                    object.put("status", "success");
                    output.writeUTF(object.toJSONString());
                    break;
                case "CreateRoom":
                    // Creates a white board session
                    String hostName = (String) object.get("clientName");
                    WhiteBoardSession newSession = new WhiteBoardSession(new Client(client, hostName));
                    sessions.put(newSession.getRoomId(), newSession);
                    newSession.start();
                    object.put("status", "success");
                    output.writeUTF(object.toJSONString());

                    break;
                case "JoinRoom":
                    // A client wants to join a room, send request to host
                    String clientName = (String) object.get("clientName");
                    int roomId =  Integer.parseInt(object.get("roomId").toString());
                    WhiteBoardSession session = sessions.get(roomId);
                    session.joinRequest(new Client(client, clientName));
                    break;
                default:
                    break;

            }

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("No request command received,");
//            e.printStackTrace();
        }
    }
}
