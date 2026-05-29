# Square Games API

Game management service for the Square Games project.

This API handles game creation, game state management, player actions, and active game tracking.

## Features

- Create games
- Play turns
- Track active games
- JWT authentication
- PostgreSQL persistence
- Plugin-based game architecture

## Supported Games

- Tic Tac Toe
- Connect Four
- Taquin (15 Puzzle)

## Technologies

- Java 21
- Spring Boot
- Spring Security
- PostgreSQL
- JWT
- Maven

## Configuration

Configure the database and JWT secret in:

`src/main/resources/application.properties`

Example:

```properties
jwt.secret=YOUR_SECRET_KEY
```

The JWT secret must be identical to the one used by the Users API.

## Running the application

```bash
mvn spring-boot:run
```

## Authentication

All endpoints require a JWT token.

Example:

```http
Authorization: Bearer YOUR_JWT_TOKEN
```

## Main Endpoints

### Create a Game

`POST /games`

### Play a Move

`POST /games/{gameId}/tokens/{tokenName}/moves`

### List Active Games

`GET /active`

## Architecture

```text
Controller
    ↓
Service
    ↓
GameDao
    ↓
JpaGameDao
    ↓
GameEntityRepository
    ↓
PostgreSQL
```

## Security

The API validates JWT tokens locally.

The authenticated user's UUID is extracted from the JWT and stored in the Spring Security context.

## Game Rules

- Only the current player can play.
- Players are identified using UUIDs.
- Active games are filtered based on the authenticated player.

## Authors

Square Games Project
