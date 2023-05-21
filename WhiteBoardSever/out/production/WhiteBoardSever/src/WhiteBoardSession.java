import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A Session maintains a set/list of sockets representing clients in the current session, one of the socket
 * is the one who initiated the white board session and thus the host.
 * It runs on a separate Thread, but the main Thread can accept incoming join requests and add the socket to
 * the current session.
 */
public class WhiteBoardSession extends Thread {
//    private ServerSocket serverSocket;
    private static int nextRoomId = 1;
    private ConcurrentHashMap<String, Client> clients;
    private Client host;
    private ConcurrentHashMap<String, Client> pendingClients;
    private JSONArray drawings;
    private int roomId;


    public WhiteBoardSession(Client host) {
        this.host = host;
        this.clients = new ConcurrentHashMap<>();
        clients.put(host.getName(), host);
        this.pendingClients = new ConcurrentHashMap<>();
        drawings = new JSONArray();
        roomId = ++nextRoomId;



    }

    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        object.put("host", host.getName());
        object.put("roomId", roomId);
        return object;
    }

    public void addClient(Client client) {
        clients.put(client.getName(), client);

        JSONObject object = new JSONObject();
        object.put("operation", "Open");
        object.put("drawings", drawings);
        try {
            client.getOutput().writeUTF(object.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void multicast(String message) {
        for(Client client : clients.values()) {
            try {
                client.getOutput().writeUTF(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        boolean sessionOpen = true;
        updateUsers();
        while(sessionOpen) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (Client client : clients.values()) {
                try {
                    DataInputStream input = client.getInput();
                    // If any client want to perform any operation
                    if (input.available() > 0) {
                        String command = input.readUTF();
                        System.out.println("command from client: " + command);
                        JSONParser parser = new JSONParser();
                        JSONObject object = (JSONObject) parser.parse(command);
                        String operation = (String) object.get("operation");
                        switch (operation) {
                            case "Draw":
                                // A draw command from a client, store it in session and multicast to other clients
                                JSONObject drawing = (JSONObject) object.get("drawing");
                                drawings.add(drawing);
                                multicast(command);
                                break;
                            case "Chat":
                                // A chat command
                                object.put("sender", client.getName());
                                multicast(object.toJSONString());
                                break;
                            case "Kick":
                                // Kick command
                                if (client == host) {
                                    String clientName = (String) object.get("clientName");
                                    Client removedClient = clients.get(clientName);
                                    if(removedClient != host) {
                                        JSONObject kickCommand = new JSONObject();
                                        kickCommand.put("operation", "Kick");
                                        removedClient.getOutput().writeUTF(kickCommand.toJSONString());
                                        clients.remove(clientName);
                                        updateUsers();
                                    }
                                }
                                break;
                            case "New":
                                // Sends new command to all clients
                                if (client == host) {
                                    multicast(command);
                                    drawings = new JSONArray();
                                }
                                break;
                            case "Open":
                                if (client == host) {
                                    multicast(command);
                                    drawings = (JSONArray) object.get("drawings");
                                }
                                break;
                            case "CloseSession":
                                if (client == host) {
                                    multicast(command);
                                    sessionOpen = false;
                                }else {
                                    clients.remove(client.getName());
                                    updateUsers();
                                }
                                break;
                            case "JoinRoom":
                                if (client == host) {
                                    String status = (String) object.get("status");
                                    String clientName = (String) object.get("clientName");
                                    Client pendingClient = pendingClients.get(clientName);
                                    pendingClients.remove(pendingClient);
                                    pendingClient.getOutput().writeUTF(command);

                                    if(status.equals("success")) {
                                        addClient(pendingClient);
                                    }
                                    updateUsers();
                                }

                            default:
                                break;
                        }

                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

        }

        WhiteBoardServer.sessions.remove(roomId);
    }

    public void updateUsers() {
        JSONObject updateUsers = new JSONObject();
        updateUsers.put("operation", "UpdateUsers");
        JSONArray users = new JSONArray();
        for(Client c : clients.values()) {
            users.add(c.getName());
        }
        updateUsers.put("users", users);
        multicast(updateUsers.toJSONString());
    }

    public int getRoomId() {
        return roomId;
    }

    public void joinRequest(Client client) {
        try {
            JSONObject request = new JSONObject();
            request.put("operation", "JoinRoom");
            request.put("clientName", client.getName());
            host.getOutput().writeUTF(request.toJSONString());
            pendingClients.put(client.getName(), client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
