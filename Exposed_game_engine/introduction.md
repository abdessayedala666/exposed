# Multiplayer Card Game Project

## 1. Game Description

**Players:** 4 players, 2v2 teams  
**Deck:** Cards numbered 1–10  
- 1–7 → 5 points each  
- 8 (Queen), 9, 10 (King) → 10 points each  

**Gameplay:**

1. Each player receives 3 cards.  
2. 4 cards are placed face-up in the middle.  
3. Players take turns playing one card at a time.  
“The 4 cards on the table are placed as a single pile, so only the top card can be captured on a turn.”

**Turn Rules:**

- If your card matches a number on the table → capture that card.  
- If an opponent previously captured cards with the same number → you can steal that stack.  
- Captured cards are kept in front of each player.  

**End of Round:**

- If a player has captured **no cards** → -50 points.  
- Otherwise → sum card values.  
- Team scores = sum of both teammates.  

**Rounds:** 3 rounds total  
- Dealer moves clockwise each round  
- Game ends after 3 rounds; highest total score wins.  

**Optional Future Feature:**  
- Wildcards / special abilities (e.g., look at enemy card, redraw, steal a stack)  
- Will be implemented **after the core game engine is stable**.

---

## 2. Project Architecture

### Layers
Frontend (Angular) ↔ WebSocket ↔ Spring Boot Server ↔ GameEngine (Pure Java)
