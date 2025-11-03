# Minecraft WebSocket API

[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.176-orange?style=for-the-badge&logo=java&labelColor=black)](https://neoforged.net/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green?style=for-the-badge&logo=minecraft&labelColor=black)](https://minecraft.net/)
[![Java](https://img.shields.io/badge/Java-21-red?style=for-the-badge&logo=openjdk&labelColor=black)](https://openjdk.org/)

A NeoForge server-side mod that exposes Minecraft functionality through a WebSocket API. Control players, manipulate worlds, and interact with blocks in real-time from external applications.

## Features

- Real-time WebSocket communication with authentication
- Optional SSL/TLS encryption support
- Modular API design with player, world, etc.
- Thread-safe concurrent operations

## Installation

### Setup

1. Download the latest JAR from [releases](https://github.com/addavriance/MinecraftWebsocketAPI/releases)
2. Place in your server's `mods` folder
3. Start the server to generate config
4. Edit `config/mcwebapi-server.toml`:

```toml
[websocket]
    #WebSocket server port
    # Default: 8765
    # Range: 1000 ~ 65535
    port = 8765
    #Authentication key for binary protocol
    authKey = "default-secret-key-change-me"
    #Enable TLS/SSL encryption
    enableSSL = false
    #Request timeout in seconds
    # Default: 30
    # Range: 1 ~ 300
    timeout = 30
    #Allowed origins for CORS
    allowedOrigins = "*"
    #WebSocket server host
    host = "0.0.0.0"
```

## Quick Start

### Python Client

Official Python client library: [mcwebapi](https://github.com/addavriance/mcwebapi)

```bash
pip install mcwebapi
```

```python
from mcwebapi import MinecraftAPI

# Connect and authenticate
api = MinecraftApi(auth_key="your-auth-key")
api.connect()

# Player operations
player = api.Player("Steve")
player.sendMessage("Hello!")
health = player.getHealth().then(print)
player.teleport(100, 64, 100)

# World operations
level = api.Level("minecraft:overworld")
level.setBlock("minecraft:diamond_block", 0, 64, 0)
level.setDayTime(6000)

# Block operations
target_block = api.Block("minecraft:overworld", 10, 64, 10)
inventory = target_block.getInventory()
```

OR

```python
from mcwebapi import MinecraftAPI

with MinecraftAPI(auth_key="your-auth-key") as client:
    while True:
        player_vec3 = client.Player("Dev").getPosition().wait()
        print(player_vec3)
```

### Protocol Format

Messages use Base64-encoded JSON (or AES-encrypted if SSL enabled):

**Request:**
```json
{
  "type": "REQUEST",
  "module": "player",
  "method": "getHealth",
  "args": ["Steve"],
  "requestId": "a1b"
}
```

**Response:**
```json
{
  "type": "RESPONSE",
  "status": "SUCCESS",
  "data": 20.0,
  "requestId": "a1b",
  "timestamp": 1699564800000
}
```

## API Modules (will be transferred to separate documentation)

### Player Module (`player`)

Comprehensive player control:

- **Health & Stats**: `getHealth`, `setHealth`, `getFood`, `setFood`, `getSaturation`
- **Position**: `getPosition`, `teleport`, `teleportToDimension`, `getRotation`, `setRotation`
- **Inventory**: `getInventory`, `clearInventory`, `giveItem`, `getArmor`, `getEnderChest`
- **Effects**: `getEffects`, `addEffect`, `clearEffects`
- **Experience**: `getExperience`, `setExperience`
- **Game Mode**: `getGameMode`, `setGameMode`
- **Advancements**: `getAdvancements`, `grantAdvancement`, `revokeAdvancement`
- **Misc**: `sendMessage`, `kick`, `getUUID`, `isOnline`, `getPing`, `getPlayerInfo`

### World Module (`level`)

World manipulation:

- **Blocks**: `getBlock`, `setBlock`, `getBlockState`
- **Time**: `getDayTime`, `setDayTime`, `getTotalTime`, `isDay`, `isNight`
- **Weather**: `getWeather`, `setWeather`
- **World Border**: `getWorldBorder`, `setWorldBorder`
- **Spawn**: `getSpawnPoint`, `setSpawnPoint`
- **Terrain**: `getHeight`, `getLightLevel`, `getMoonPhase`
- **Chunks**: `getChunkInfo`, `loadChunk`, `unloadChunk`
- **Entities**: `getPlayers`, `getEntities`, `getEntityCount`
- **Actions**: `sendMessageToAll`, `explode`
- **Info**: `getAvailableLevels`, `getLevelInfo`, `getDifficulty`, `setDifficulty`

### Block Module (`block`)

Block entity operations:

- **Info**: `getBlock` - detailed block state with properties
- **Manipulation**: `setBlock`, `breakBlock`, `fillArea` (max 10k blocks)
- **Containers**: `getInventory`, `setInventorySlot`, `clearInventory`
- **Furnaces**: `getFurnaceInfo` - burn time, cook progress, slots

## Documentation

Complete API reference: [Wiki Documentation](https://github.com/addavriance/MinecraftWebsocketAPI/wiki) (WIP)

## Security

- **Change default auth key immediately**
- Enable SSL for production: Set `enableSSL = true` and ensure proper key setup (WIP)
- Bind to specific interface: Use `host = "127.0.0.1"` for local-only access
- Use firewall rules to restrict port access
- Monitor logs for unauthorized attempts

## Building

```bash
git clone https://github.com/addavriance/MinecraftWebsocketAPI.git
cd MinecraftWebsocketAPI
./gradlew build
# Output: build/libs/mcwebapi-1.3.0.jar
```

## Troubleshooting

**Port already in use**
- Change `port` in config or stop conflicting service

**Authentication fails**
- Verify auth key matches between server config and client
- Check SSL settings match on both sides

**Stale data**
- Player cache TTL: 30 seconds
- World cache TTL: 60 seconds
- Caches auto-clear on player logout/dimension change

**Performance issues**
- Reduce concurrent connections
- Increase cache TTL if needed
- Monitor server TPS

## Performance Notes

- Smart caching with SoftReferences prevents memory leaks
- Concurrent data structures ensure thread safety
- Non-blocking WebSocket implementation
- Typical overhead: <1% server tick time with moderate usage

## Known Limitations

- Server-side only (no client installation needed)
- SSL uses AES/ECB - adequate for most use cases, not cryptographically perfect, but WIP

## Contributing

Issues and pull requests welcome at [GitHub](https://github.com/addavriance/MinecraftWebsocketAPI)

## Links

- **Python Client**: [minecraft-websocket-client](https://github.com/addavriance/mcwebapi)
- **Documentation**: [Wiki](https://github.com/addavriance/MinecraftWebsocketAPI/wiki)
- **Issues**: [GitHub Issues](https://github.com/addavriance/MinecraftWebsocketAPI/issues)

---

Built with [NeoForge](https://neoforged.net/), [Jackson](https://github.com/FasterXML/jackson), and [Netty](https://netty.io/)
