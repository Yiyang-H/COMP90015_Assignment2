package com.example.whiteboardclient;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * A singleton class used to send non-persistent request to the server
 */
public class Client {
    private String serverAddress;
    private int serverPort;
    private String clientName;
    private static Client _instance;

    public Client(String serverAddress, int serverPort, String clientName) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.clientName = clientName;
        _instance = this;
    }

    public static Client getInstance() {
        return _instance;
    }


    public Socket createRoom() {
        Socket s = null;
        try {
            s = new Socket(serverAddress, serverPort);
            DataOutputStream output = new DataOutputStream(s.getOutputStream());
            DataInputStream input = new DataInputStream(s.getInputStream());

            JSONObject request = new JSONObject();
            request.put("operation", "CreateRoom");
            request.put("clientName", clientName);
            output.writeUTF(request.toJSONString());
            String response = input.readUTF();
            System.out.println(response);


        } catch (IOException e) {
            e.printStackTrace();
        }

        return s;
    }

    public Socket joinRoom(int roomId) {
        Socket s = null;
        try {
            s = new Socket(serverAddress, serverPort);
            DataOutputStream output = new DataOutputStream(s.getOutputStream());
            DataInputStream input = new DataInputStream(s.getInputStream());

            JSONObject request = new JSONObject();
            request.put("operation", "JoinRoom");
            request.put("clientName", clientName);
            request.put("roomId", roomId);
            // The client sends the request, waiting for host to accept
            output.writeUTF(request.toJSONString());
            String response = input.readUTF();
            System.out.println("Res: " + response);
            JSONParser parser = new JSONParser();
            JSONObject responseJSON = (JSONObject) parser.parse(response);
            if(responseJSON.get("status").equals("success")) {
                return s;
            }else {
                return null;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return s;
    }

    public JSONArray getRooms(){
        JSONObject request =  new JSONObject();
        request.put("operation", "Rooms");
        JSONObject response = deliverMessage(request.toJSONString());
        if(response != null) {
            JSONArray rooms = (JSONArray) response.get("rooms");
            return rooms;
        }

        return null;
    }

    public JSONObject deliverMessage(String message) {
        try {
            Socket s = new Socket(serverAddress, serverPort);
            DataOutputStream output = new DataOutputStream(s.getOutputStream());
            DataInputStream input = new DataInputStream(s.getInputStream());
            output.writeUTF(message);
            String response = input.readUTF();
            JSONParser parser = new JSONParser();
            JSONObject responseJSON = (JSONObject) parser.parse(response);
            if(((String)responseJSON.get("status")).equals("success")) {
                return responseJSON;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Failed to connect to the server!");
        }

        return null;

    }
}
