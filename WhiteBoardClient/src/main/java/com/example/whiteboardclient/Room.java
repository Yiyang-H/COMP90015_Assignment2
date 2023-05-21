package com.example.whiteboardclient;

/**
 * A room contains a name and an id
 */
public class Room {


    private String name;
    private int id;

    public Room(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getRoomId() {
        return id;
    }


}
