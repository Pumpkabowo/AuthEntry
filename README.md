# AuthEntry

**AuthEntry** is a Minecraft paper plugin designed to enhance server access control by requiring new or unverified players to answer a predefined security question before gaining full interaction privileges. Until verification is complete, players are restricted from chatting, moving, or interacting.

## Features

- Enforces an identity challenge (e.g., "What is Ramen's Discord username?")
- Temporarily restricts:
  - Chat messages
  - Player movement
  - World interaction
  - Damage reception
- Configurable list of accepted answers
- Admin commands for manual verification management
- Persists verified player data via `config.yml`

## Commands

| Command                      | Description                                       |
|-----------------------------|---------------------------------------------------|
| `/auth <player>`            | Toggles verification status for a player         |
| `/auth question <player>`   | Sends the security question to a specific player |
| `/authplayers`              | Lists verified and pending players               |

## Configuration

- Verified players are stored as UUIDs under the `verified` key in `config.yml`
- Accepted answers are defined within the source code (`acceptedAnswers` list)

## Installation

1. Place `AuthEntry.jar` into your server's `/plugins` directory
2. Restart or reload the server
3. The plugin will auto-generate `config.yml` if not present

## Requirements

- Minecraft Server: Paper 1.16–1.21.5 (tested with 1.21.5)
- Java Runtime: Java 17 or higher

Last Updated: 6/2/2025
