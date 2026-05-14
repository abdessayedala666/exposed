card-game/
│
├ engine/ ← pure Java game logic
│ ├ Card.java
│ ├ Deck.java
│ ├ Player.java
│ ├ Team.java
│ ├ GameState.java
│ ├ RoundState.java
│ ├ GameEngine.java
│ └ ScoreCalculator.java
│
├ simulation/ ← optional main() or console runner for testing engine
│ └ GameSimulatorMain.java
│
├ server/ ← Spring Boot backend
│ ├ GameRoom.java
│ ├ GameRoomManager.java
│ ├ WebSocketController.java
│ └ CardGameServerApplication.java
│
└ frontend/ ← Angular UI
├ components/
├ services/
└ app.module.ts