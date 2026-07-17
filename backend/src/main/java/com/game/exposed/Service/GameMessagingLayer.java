package com.game.exposed.Service;

import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.game.exposed.dto.MoveResult;

@Service
public class GameMessagingLayer {
    private final SimpMessagingTemplate messagingTemplate;

    public GameMessagingLayer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public Map<String, MoveResult> sendLaunchSnapshots(Map<String, MoveResult> launchSnapshotsByPlayerName) {
        if (launchSnapshotsByPlayerName == null || launchSnapshotsByPlayerName.isEmpty()) {
            return launchSnapshotsByPlayerName;
        }

        launchSnapshotsByPlayerName.forEach((playerName, snapshotDto) -> {
            if (playerName == null || snapshotDto == null) {
                return;
            }

            messagingTemplate.convertAndSendToUser(
                    playerName,
                    "/queue/game",
                    snapshotDto);
        });
        return launchSnapshotsByPlayerName;
    }

    public Map<String, MoveResult> sendMoveSnapshots(Map<String, MoveResult> moveSnapshotsByPlayerName) {
        if (moveSnapshotsByPlayerName == null || moveSnapshotsByPlayerName.isEmpty()) {
            return moveSnapshotsByPlayerName;
        }

        moveSnapshotsByPlayerName.forEach((playerName, snapshotDto) -> {
            if (playerName == null || snapshotDto == null) {
                return;
            }

            messagingTemplate.convertAndSendToUser(
                    playerName,
                    "/queue/move",
                    snapshotDto);
        });

        return moveSnapshotsByPlayerName;
    }

    public void sendMoveError(String playerName, String errorMessage) {
        if (playerName == null || playerName.isBlank() || errorMessage == null || errorMessage.isBlank()) {
            return;
        }

        messagingTemplate.convertAndSendToUser(
                playerName,
                "/queue/move-error",
                Map.of("message", errorMessage));
    }

    public void sendLaunchError(String playerName, String errorMessage) {
        if (playerName == null || playerName.isBlank() || errorMessage == null || errorMessage.isBlank()) {
            return;
        }

        messagingTemplate.convertAndSendToUser(
                playerName,
                "/queue/game-error",
                Map.of("message", errorMessage));
    }

    public void sendRoomError(String playerName, String errorMessage) {
        if (playerName == null || playerName.isBlank() || errorMessage == null || errorMessage.isBlank()) {
            return;
        }

        messagingTemplate.convertAndSendToUser(
                playerName,
                "/queue/room-error",
                Map.of("message", errorMessage));
    }
}