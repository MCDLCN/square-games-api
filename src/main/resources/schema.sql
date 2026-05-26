DROP TABLE IF EXISTS game_tokens;
DROP TABLE IF EXISTS game_players;
DROP TABLE IF EXISTS games;

CREATE TABLE games (
                       id UUID PRIMARY KEY,
                       game_type VARCHAR(50) NOT NULL,
                       board_size INTEGER NOT NULL,
                       current_player_id UUID,
                       status VARCHAR(50) NOT NULL
);

CREATE TABLE game_players (
                              id SERIAL PRIMARY KEY,
                              game_id UUID NOT NULL REFERENCES games(id) ON DELETE CASCADE,
                              player_id UUID NOT NULL,
                              player_order INTEGER NOT NULL
);

CREATE TABLE game_tokens (
                             id SERIAL PRIMARY KEY,
                             game_id UUID NOT NULL REFERENCES games(id) ON DELETE CASCADE,
                             name VARCHAR(50) NOT NULL,
                             owner_id UUID,
                             removed BOOLEAN NOT NULL,
                             x INTEGER,
                             y INTEGER
);