package com.game.exposed.Repository;
import org.springframework.data.repository.CrudRepository;

import com.game.exposed.models.RoomSeat;

public interface  RoomseatRepository extends CrudRepository<RoomSeat, Integer> {
    RoomSeat findByRoomIdAndSeatIndex(String roomId, Integer seatIndex);

}
