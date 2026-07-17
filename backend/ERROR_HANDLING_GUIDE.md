# Error Handling System Documentation

## Overview
A comprehensive, centralized error handling system has been implemented for your Spring Boot application. This system ensures that:
- **No NullPointerExceptions crash the server** - they're caught and converted to meaningful API responses
- **All exceptions return consistent JSON responses** - clients receive structured error information
- **The application remains stable** - errors are handled gracefully without shutting down
- **Debugging is easier** - all errors are properly logged with stack traces

## Architecture

### 1. ApiError DTO (JSON Response Model)
**Location:** [src/main/java/com/game/exposed/dto/ApiError.java](src/main/java/com/game/exposed/dto/ApiError.java)

The standardized error response format:
```json
{
  "status": 400,
  "message": "Username is not set in the session",
  "error": "Missing Session Data",
  "path": "/guest",
  "timestamp": "2026-04-11T12:30:45.123456",
  "details": null
}
```

**Fields:**
- `status` - HTTP status code
- `message` - Detailed error message for the client
- `error` - Error category (e.g., "Bad Request", "Not Found")
- `path` - The API endpoint where the error occurred
- `timestamp` - When the error occurred
- `details` - Optional additional information

### 2. Custom Exception Classes

#### ResourceNotFoundException
**Location:** [Exceptions/ResourceNotFoundException.java](src/main/java/com/game/exposed/Exceptions/ResourceNotFoundException.java)

**Used for:** When a requested resource (room, user, etc.) is not found
**HTTP Status:** 404 Not Found
**Example message:** "Room not found: ABC123"

#### InvalidOperationException
**Location:** [Exceptions/InvalidOperationException.java](src/main/java/com/game/exposed/Exceptions/InvalidOperationException.java)

**Used for:** When an operation cannot be completed (seat taken, game not in valid state, etc.)
**HTTP Status:** 409 Conflict
**Example message:** "Seat already taken"

#### MissingSessionDataException
**Location:** [Exceptions/MissingSessionDataException.java](src/main/java/com/game/exposed/Exceptions/MissingSessionDataException.java)

**Used for:** When required session data is missing (username, session ID, etc.)
**HTTP Status:** 401 Unauthorized
**Example message:** "Username is not set in the session"

#### UsernameNotFoundException
**Location:** [Exceptions/UsernameNotFoundException.java](src/main/java/com/game/exposed/Exceptions/UsernameNotFoundException.java)

**Used for:** When a specific user doesn't exist
**HTTP Status:** 404 Not Found
**Example message:** "this user doesnt exist"

### 3. Global Exception Handler
**Location:** [Config/GlobalExceptionHandler.java](src/main/java/com/game/exposed/Config/GlobalExceptionHandler.java)

This `@RestControllerAdvice` intercepts all exceptions thrown in the application and converts them to appropriate JSON responses. It handles:

| Exception | HTTP Status | Category |
|-----------|------------|----------|
| `ResourceNotFoundException` | 404 | Resource Not Found |
| `UsernameNotFoundException` | 404 | User Not Found |
| `InvalidOperationException` | 409 | Invalid Operation |
| `MissingSessionDataException` | 401 | Missing Session Data |
| `IllegalArgumentException` | 400 | Invalid Argument |
| `IllegalStateException` | 409 | Invalid State |
| `NullPointerException` | 500 | Null Pointer Error |
| `NumberFormatException` | 400 | Number Format Error |
| Generic `RuntimeException` | 500 | Runtime Error |
| Generic `Exception` | 500 | Server Error |

## Updated Controllers

### GuestController
**Changes:**
- Added input validation for name (null/empty check)
- Added session validation
- Uses `MissingSessionDataException` for missing session
- Uses `IllegalArgumentException` for invalid input

### RoomController
**Changes:**
- Added validation for room IDs
- Added session validation before operations
- Uses `MissingSessionDataException` for missing username
- Uses `ResourceNotFoundException` for non-existent rooms
- Validates all path variables before use

### RoomSocketController
**Changes:**
- Validates all incoming payloads (null check)
- Validates session attributes
- Uses `MissingSessionDataException` for missing session data
- Uses `InvalidOperationException` for failed operations
- Properly handles null checks before accessing map values

## Updated Services

### RoomService
**Changes:**
- All parameters validated for null/empty values
- Null checks before accessing objects (rooms.get(roomId))
- Uses `ResourceNotFoundException` for non-existent rooms
- Uses `InvalidOperationException` for invalid operations
- All methods protect against NullPointerException

**Key improvements:**
```java
// Before - Could throw NullPointerException
Map<Integer, String> seats = rooms.get(roomId).getSeats();

// After - Throws meaningful exception
Room room = rooms.get(roomId);
if (room == null) {
    throw new ResourceNotFoundException("Room not found: " + roomId);
}
Map<Integer, String> seats = room.getSeats();
```

### GameService
**Changes:**
- Validates all input parameters (name, type, roomId)
- Null checks for Room object before accessing properties
- Null checks for player objects and hands
- Uses `ResourceNotFoundException` for non-existent rooms
- Uses `InvalidOperationException` for invalid game states
- Comprehensive logging for debugging

**Key improvements:**
```java
// Before - Could crash if room is null
Room room = rooms.get(roomId);
if (!room.getRoomOwner().equals(name)) { ... }

// After - Safe null handling
Room room = rooms.get(roomId);
if (room == null) {
    throw new ResourceNotFoundException("Room not found: " + roomId);
}
String roomOwner = room.getRoomOwner();
if (roomOwner == null) {
    throw new InvalidOperationException("Room owner not set");
}
if (!roomOwner.equals(name)) { ... }
```

## Exception Flow

```
Client Request
    ↓
Controller (with validation)
    ↓
Service (with null checks & validation)
    ↓
Exception thrown
    ↓
GlobalExceptionHandler (@RestControllerAdvice)
    ↓
Converts to ApiError JSON
    ↓
Returns with appropriate HTTP status
    ↓
Server remains running ✓
Client receives error details ✓
Stack trace logged for debugging ✓
```

## Usage Examples

### Example 1: Missing Username
```
Request: POST /guest
Body: { "name": "" }

Response:
Status: 400 Bad Request
{
  "status": 400,
  "message": "Name cannot be null or empty",
  "error": "Invalid Argument",
  "path": "/guest",
  "timestamp": "2026-04-11T12:30:45.123456"
}
```

### Example 2: Room Not Found
```
Request: GET /room/INVALID123/owner

Response:
Status: 404 Not Found
{
  "status": 404,
  "message": "Room not found: INVALID123",
  "error": "Resource Not Found",
  "path": "/room/INVALID123/owner",
  "timestamp": "2026-04-11T12:30:45.123456"
}
```

### Example 3: Null Pointer Exception (caught & handled)
```
Request: POST /room/create
(Session name attribute is null)

Response:
Status: 401 Unauthorized
{
  "status": 401,
  "message": "Username is not set in the session",
  "error": "Missing Session Data",
  "path": "/room/create",
  "timestamp": "2026-04-11T12:30:45.123456"
}
```

### Example 4: Invalid Seat Index
```
Request: WebSocket /room/ABC123/seats
Payload: { "action": "join", "index": 5 }

Response:
Status: 400 Bad Request
{
  "status": 400,
  "message": "Invalid seat index: 5",
  "error": "Invalid Argument",
  "path": "/room/ABC123/seats",
  "timestamp": "2026-04-11T12:30:45.123456"
}
```

## Null Pointer Protection

The following methods now have comprehensive null checks:

### Room Operations
- ✓ `createRoom()` - validates sessionId and name
- ✓ `getOwner()` - validates roomId, checks Room exists, checks owner exists
- ✓ `join()` - validates all parameters, checks Room/Seats exist
- ✓ `leave()` - validates parameters, checks Room/Seats exist
- ✓ `getSeats()` - validates roomId, checks Room/Seats exist
- ✓ `roomExistance()` - safely returns false for invalid roomId

### Guest Operations
- ✓ `saveGuest()` - validates request and session
- ✓ `getGuest()` - validates session

### Game Operations
- ✓ `launchGame()` - validates all inputs, checks for null Room/Players/Seats/Engine
- ✓ Creates safe player maps with null checks

## Debugging

When an error occurs, the stack trace is logged to console:
```
java.lang.NullPointerException: null
    at com.game.exposed.Service.RoomService.getOwner(RoomService.java:35)
    at com.game.exposed.Controllers.RoomController.getOwner(RoomController.java:92)
    ...
```

This appears in your application logs while the client receives a clean JSON error response.

## Testing the Error Handling

You can test the system with curl:

```bash
# Test missing name
curl -X POST http://localhost:8080/guest \
  -H "Content-Type: application/json" \
  -d '{"name": ""}'

# Test invalid room
curl -X GET http://localhost:8080/room/INVALID/owner

# Test invalid seat index
# (via WebSocket - send: { "action": "join", "index": 10 })
```

## Best Practices Going Forward

1. **Always validate inputs** before using them:
   ```java
   if (roomId == null || roomId.isBlank()) {
       throw new IllegalArgumentException("Room ID cannot be null or empty");
   }
   ```

2. **Check for null before accessing object properties:**
   ```java
   Room room = rooms.get(roomId);
   if (room == null) {
       throw new ResourceNotFoundException("Room not found: " + roomId);
   }
   String owner = room.getRoomOwner();
   ```

3. **Use specific exception types** to communicate what went wrong:
   - `ResourceNotFoundException` - when something doesn't exist
   - `InvalidOperationException` - when an operation can't be done
   - `MissingSessionDataException` - when session data is missing
   - `IllegalArgumentException` - when input is invalid

4. **Never catch exceptions silently** unless you have a specific recovery strategy

5. **Log errors for debugging** (already done by GlobalExceptionHandler)

## Files Created/Modified

**New Files:**
- [dto/ApiError.java](src/main/java/com/game/exposed/dto/ApiError.java)
- [Exceptions/ResourceNotFoundException.java](src/main/java/com/game/exposed/Exceptions/ResourceNotFoundException.java)
- [Exceptions/InvalidOperationException.java](src/main/java/com/game/exposed/Exceptions/InvalidOperationException.java)
- [Exceptions/MissingSessionDataException.java](src/main/java/com/game/exposed/Exceptions/MissingSessionDataException.java)
- [Config/GlobalExceptionHandler.java](src/main/java/com/game/exposed/Config/GlobalExceptionHandler.java)

**Modified Files:**
- [Controllers/GuestController.java](src/main/java/com/game/exposed/Controllers/GuestController.java)
- [Controllers/RoomController.java](src/main/java/com/game/exposed/Controllers/RoomController.java)
- [Controllers/RoomSocketController.java](src/main/java/com/game/exposed/Controllers/RoomSocketController.java)
- [Service/RoomService.java](src/main/java/com/game/exposed/Service/RoomService.java)
- [Service/GameService.java](src/main/java/com/game/exposed/Service/GameService.java)

## Summary

Your application now has:
- ✅ **Centralized error handling** - all errors handled in one place
- ✅ **NullPointerException protection** - comprehensive null checks
- ✅ **Consistent error responses** - all errors return structured JSON
- ✅ **Server stability** - errors don't crash the application
- ✅ **Better debugging** - stack traces logged, errors categorized
- ✅ **Client-friendly** - meaningful error messages
- ✅ **Extensible** - easy to add more specific exceptions

The server will now gracefully handle errors and continue running!
