package com.game.exposed.Repository;

import com.game.exposed.models.Room;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RoomRepository extends CrudRepository<Room, Integer> {

    Optional<Room> findByRoomId(String roomId);
}