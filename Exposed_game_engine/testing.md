
---

## 5. Testing Strategy

- **Engine:** test via console runner (`GameSimulatorMain`) and **unit tests (JUnit)**  
- **Server:** test with mock WebSocket events  
- **Frontend:** updates UI based on server events  

---

## 6. Development Roadmap

**Phase 1:**  
- Implement core engine: deck, dealing, turns, captures, scoring, rounds  

**Phase 2:**  
- Test engine with simulator & unit tests  

**Phase 3:**  
- Build Spring Boot server → manage multiple GameRooms → integrate engine  

**Phase 4:**  
- Build Angular frontend → connect via WebSocket  

**Phase 5 (Optional):**  
- Add wildcard cards / special abilities  