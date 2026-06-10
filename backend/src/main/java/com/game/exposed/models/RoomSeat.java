package com.game.exposed.models;

import jakarta.persistence.*;

@Entity
public class RoomSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    private Integer seatIndex; // 0–3

    private String playerToken;

    private String playerName;

    public RoomSeat(Room room , Integer seatIndex ) {
        this.room = room;
        this.seatIndex = seatIndex;
        this.playerName = "Player " + (seatIndex + 1);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRoomId() {
        return room != null ? room.getRoomId() : null;
    }

 
    public void setRoom(Room room) {
        this.room = room;
    }
    

    public Integer getSeatIndex() {
        return seatIndex;
    }

    public void setSeatIndex(Integer seatIndex) {
        this.seatIndex = seatIndex;
    }

    public String getPlayerToken() {
        return playerToken;
    }

    public void setPlayerToken(String playerToken) {
        this.playerToken = playerToken;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}