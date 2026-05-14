
**Responsibilities:**

- **Frontend (Angular):** displays cards, animations, player input  
- **Spring Boot Server:** manages game rooms, receives moves, calls engine, sends results to clients  
- **Game Engine (Java):** pure game logic, manages GameState, applies rules, calculates scores

---

### Game Flow

1. Player creates or joins a room.  
2. Server creates a **GameRoom** → instantiates a **GameEngine**.  
3. Engine creates **GameState**: deck, players, teams, table cards, scores, current turn.  
4. Players take turns → engine validates moves, updates state, returns **MoveResult** to server.  
5. Server forwards results to frontend → UI updates.  
6. Round ends → engine calculates scores → next round starts.  
7. Game ends after 3 rounds → winner determined.  

**Notes:**  
- Engine enforces **all rules**.  
- Frontend only receives **events / MoveResult**, never modifies state directly.  
- Each game has **its own GameEngine and GameState**, fully independent in server memory.  

---

## 3. Engine Design

**Core Classes:**
engine/
├ Card
├ Deck
├ Player
├ Team
├ GameState // single source of truth for a game
├ RoundState
├ GameEngine // manages rules and game flow
└ ScoreCalculator

**Optional Future Classes for Wildcards:**
abilities/
├ Ability
├ SpyAbility
├ DrawAbility
└ StealAbility

**GameEngine Responsibilities:**

- Initialize game and rounds  
- Manage turns  
- Validate moves  
- Resolve captures / steals  
- Apply wildcard effects (future)  
- Update GameState  
- Calculate round and team scores  
- Generate events/results for server → frontend

**GameState Contents:**

- Players & Teams  
- Current Round  
- Current Player Turn  
- Deck & Table Cards  
- Captured Stacks  
- Scores  

---


