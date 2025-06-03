# AuthEntry

**AuthEntry** is a Minecraft plugin designed to enhance server access control by requiring new or unverified players to answer a predefined security question before gaining full interaction privileges. Until verification is complete, players are restricted from chatting, moving, or interacting.

## Features

- Enforces an identity challenge (e.g., security question)
- Temporarily restricts:
  - Chat messages
  - Player movement
  - World interaction
  - Damage reception
    
- Configurable list of accepted answers
- Admin commands for manual verification management
- Persists verified player data via `verified.yml`


## Commands

| Command                      | Description                                       |
|-----------------------------|---------------------------------------------------|
| `/auth <player>`            | Toggles on/off verification status for a player  |
| `/auth list`              | Lists verified and pending players               |
| `/auth enable/disable`      | Enable and disable the security freeze           |
| `/auth status`              | Checks to see if the plugin is enabled/disabled  |

## Configuration

- Verified players are stored as UUIDs in `verified.yml`
- Accepted answers are defined within the `config.yml`
- Costomize messages in `config.yml`

## Installation

1. Place `AuthEntry.jar` into your server's `/plugins` directory
2. Restart or reload the server
3. The plugin will auto-generate `config.yml` if not present

## Requirements
- Minecraft Server: Paper 1.16–1.21.5 (tested with 1.21.5)
- Java Runtime: Java 17 or higher

## Images

- Welcome Message:

![image](https://github.com/user-attachments/assets/7ccbe597-1e61-4436-9be6-4de3240ee9b7)

- Correct Answer:

![image](https://github.com/user-attachments/assets/24900eab-2968-4a0d-a8d6-409eb37ab8ca)

- Incorrect Answer:

![image](https://github.com/user-attachments/assets/47e0e4f6-28c4-467a-bfb2-f65aa0e10b9c)

- Command List:

![image](https://github.com/user-attachments/assets/8f77434c-eea1-4803-a8fc-898f5d256267)

- Verified Players:

![image](https://github.com/user-attachments/assets/889d241c-1940-4d41-b72b-0de277272a44)

- Enabled/Disabled
 
![image](https://github.com/user-attachments/assets/96554c9c-4ddd-4223-8ed7-d1fce857a530)

- Unauthrozied
  
![image](https://github.com/user-attachments/assets/d9537747-5e76-4aac-a893-af033e185f8c)


Last Updated: 6/2/2025
