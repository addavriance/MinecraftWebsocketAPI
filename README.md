<div align="center">
  <img width="200" height="200" alt="mcwebapi" src="https://github.com/user-attachments/assets/8a9bd1e8-9a64-4e9b-a949-e556069e1a4f" />
  
  <h1>Minecraft WebSocket API</h1>
  
  <p>
    <strong>Control Minecraft in real-time from any programming language</strong>
  </p>
  
  <p>
    <a href="https://pypi.org/project/mcwebapi/">
      <img src="https://img.shields.io/pypi/v/mcwebapi?style=for-the-badge&logo=pypi&logoColor=white&label=PyPI" alt="PyPI"/>
    </a>
    <a href="https://github.com/addavriance/MinecraftWebsocketAPI">
      <img src="https://img.shields.io/badge/NeoForge-1.21.1-orange?style=for-the-badge&logo=java&labelColor=black" alt="NeoForge"/>
    </a>
    <a href="https://github.com/addavriance/MinecraftWebsocketAPI/blob/main/LICENSE">
      <img src="https://img.shields.io/badge/License-GPL--3.0-blue?style=for-the-badge" alt="License"/>
    </a>
  </p>
  
  <p>
    <a href="#features">Features</a> •
    <a href="#installation">Installation</a> •
    <a href="#quick-start">Quick Start</a> •
    <a href="#api-modules">API Modules</a> •
    <a href="#documentation">Docs</a>
  </p>
  
</div>

<br/>

<!-- Separator -->
<p align="center">
  <img src="https://user-images.githubusercontent.com/73097560/115834477-dbab4500-a447-11eb-908a-139a6edaec5c.gif" width="100%"/>
</p>

## What is this?

A **server-side NeoForge mod** that exposes Minecraft's functionality through a **WebSocket API**, enabling real-time control from external applications in **any programming language**.

```python
# It's THIS simple:
from mcwebapi import MinecraftAPI

with MinecraftAPI() as api:
    api.Player("Steve").teleport(0, 100, 0).wait()
    api.Level("minecraft:overworld").setDayTime(6000).wait()
    api.Block("minecraft:overworld", 10, 64, 10).setBlock("minecraft:diamond_block").wait()
```

**Perfect for:** Automation • Education • Custom Game Mechanics • Server Monitoring • Testing • Fun! 

---

## Features

<div align="center">
<table>
  <tr>
    <td width="50%">
      
### Real-time Communication
      
- **WebSocket-based** for instant responses
- **Thread-safe** concurrent operations  
- **Non-blocking** implementation
- **<1% overhead** on server tick time

    </td>
    <td width="50%">
      
### Secure & Flexible

- **Token authentication**
- **Optional SSL/TLS** encryption
- **CORS support**
- **Configurable** timeouts & origins

    </td>
  </tr>
  <tr>
    <td width="50%">
      
### Comprehensive API

- **Player operations** (health, inventory, teleport)
- **World manipulation** (blocks, time, weather)
- **Block entities** (chests, furnaces)
- **Full NBT support**

    </td>
    <td width="50%">
      
### Python Client

- **PyPI package** (`pip install mcwebapi`)
- **Promise-based** async patterns
- **Type hints** for IDE support
- **Context managers** for clean code

    </td>
  </tr>
</table>
</div>

---

## Installation

### Server (Minecraft Mod)

1. **Download** the latest JAR from [releases](https://github.com/addavriance/MinecraftWebsocketAPI/releases)
2. **Place** in your server's `mods/` folder
3. **Start** the server to generate config OR join signleplayer world
4. **Edit** `config/mcwebapi-server.toml`:

```toml
[websocket]
    port = 8765
    authKey = "your-secret-key-here"  # ⚠️ CHANGE THIS!
    enableSSL = false
    host = "0.0.0.0"
```

### Client (Python)

```bash
pip install mcwebapi
```

---

## Quick Start

### Basic Example

```python
from mcwebapi import MinecraftAPI

# Connect to your server
api = MinecraftAPI(
    host="localhost",
    port=8765,
    auth_key="your-secret-key-here"
)

api.connect()

# Control players
player = api.Player("Steve")
player.setHealth(20.0)
player.teleport(0, 100, 0)
player.sendMessage("Hello from Python!")

# Manipulate world
level = api.Level("minecraft:overworld")
level.setDayTime(6000)  # Noon
level.setWeather(True, False)  # Rain, no thunder

# Interact with blocks
block = api.Block("minecraft:overworld", 10, 64, 10)
block.setBlock("minecraft:diamond_block")

api.disconnect()
```

### Context Manager (Recommended)

```python
from mcwebapi import MinecraftAPI

with MinecraftAPI(auth_key="your-key") as api:
    # Your code here
    position = api.Player("Steve").getPosition().wait()
    print(f"Steve is at {position}")
    
# Automatically disconnects and cleans up
```

### Async with Promises

```python
from mcwebapi import MinecraftAPI

api = MinecraftAPI()
api.connect()

# Non-blocking with callbacks
api.Player("Steve").getHealth().then(lambda hp: print(f"Health: {hp}"))
api.Player("Alex").getPosition().then(lambda pos: print(f"Position: {pos}"))

# Or wait synchronously
health = api.Player("Steve").getHealth().wait()

api.wait_for_pending()
api.disconnect()
```

---

## API Modules

<details>
<summary><b>Player Module</b> - Comprehensive player control</summary>

<br/>

**Health & Stats**
- `getHealth()`, `setHealth(hp)`, `getMaxHealth()`
- `getFood()`, `setFood(level)`, `getSaturation()`

**Position & Movement**
- `getPosition()` → `{x, y, z}`
- `teleport(x, y, z)`
- `teleportToDimension(dimension, x, y, z)`
- `getRotation()`, `setRotation(yaw, pitch)`
- `getVelocity()`, `setVelocity(x, y, z)`

**Inventory & Items**
- `getInventory()` → List of items with NBT
- `clearInventory()`
- `giveItem(itemId, count)`
- `getArmor()`, `getEnderChest()`

**Effects & Experience**
- `getEffects()`, `addEffect(effect, duration, amplifier)`, `clearEffects()`
- `getExperience()`, `setExperience(level)`

**Game Mode & Info**
- `getGameMode()`, `setGameMode(mode)`
- `getPlayerInfo()` → Comprehensive stats
- `getUUID()`, `isOnline()`, `getPing()`

**Actions**
- `sendMessage(text)`
- `kick(reason)`

</details>

<details>
<summary><b>World Module</b> - World manipulation</summary>

<br/>

**Blocks**
- `getBlock(x, y, z)` → Block type + properties
- `setBlock(blockId, x, y, z)`
- `getBlockState(x, y, z)` → Full state with properties

**Time & Weather**
- `getDayTime()`, `setDayTime(time)`
- `isDay()`, `isNight()`, `getMoonPhase()`
- `getWeather()`, `setWeather(rain, thunder)`

**Terrain**
- `getHeight(x, z, heightmapType)`
- `getLightLevel(x, y, z)`
- `getSpawnPoint()`, `setSpawnPoint(x, y, z, angle)`

**World Border**
- `getWorldBorder()` → Center, size, damage
- `setWorldBorder(centerX, centerZ, size)`

**Entities & Players**
- `getPlayers()` → List of online players
- `getEntities()`, `getEntityCount()`
- `sendMessageToAll(message)`

**Chunks**
- `getChunkInfo(chunkX, chunkZ)`
- `loadChunk(chunkX, chunkZ)`
- `unloadChunk(chunkX, chunkZ)`

**Advanced**
- `explode(x, y, z, power, fire)`
- `getDifficulty()`, `setDifficulty(difficulty)`
- `getLevelInfo()` → Complete world stats

</details>

<details>
<summary><b>Block Module</b> - Block entity operations</summary>

<br/>

**Block Info**
- `getBlock()` → Type, properties, light levels, block entity info

**Manipulation**
- `setBlock(blockId)`
- `breakBlock(dropItems)`

**Containers (Chests, Barrels, etc.)**
- `getInventory()` → Items with full NBT data
- `setInventorySlot(slot, itemId, count)`
- `clearInventory()`

**Furnaces**
- `getFurnaceInfo()` → Burn time, cook progress, slots

</details>

---

## Documentation

- **[Full API Reference](https://github.com/addavriance/MinecraftWebsocketAPI/wiki)** (WIP)
- **[Python Client Docs](https://github.com/addavriance/mcwebapi)**
- **[Discord Community](https://discord.gg/your-invite)** (optional)

---

## Security

**Important Security Notes:**

1. **Change the default auth key** immediately in config
2. **Use SSL/TLS** in production (set `enableSSL = true`)
3. **Bind to localhost** if not exposing externally (`host = "127.0.0.1"`)
4. **Firewall rules** to restrict port access
5. **Monitor logs** for unauthorized attempts

---

## Building from Source

```bash
git clone https://github.com/addavriance/MinecraftWebsocketAPI.git
cd MinecraftWebsocketAPI
./gradlew build
```

Output: `build/libs/mcwebapi-1.3.0.jar`

---

## Troubleshooting

<details>
<summary><b>Port already in use</b></summary>
Change `port` in config or stop the conflicting service.
</details>

<details>
<summary><b>Authentication fails</b></summary>
Verify that `authKey` matches between server config and client code.
</details>

<details>
<summary><b>Stale data / caching issues</b></summary>

- Player cache TTL: 30 seconds
- World cache TTL: 60 seconds  
- Caches auto-clear on logout/dimension change
</details>

<details>
<summary><b>Performance issues</b></summary>

- Reduce concurrent connections
- Monitor server TPS
- Check network latency
</details>

---

## Performance

- **Smart caching** with SoftReferences (prevents memory leaks)
- **Thread-safe** concurrent data structures
- **Non-blocking** WebSocket implementation
- **<1% overhead** on server tick time with moderate usage

---

## Contributing

Contributions are welcome! Please feel free to:

- Report bugs via [Issues](https://github.com/addavriance/MinecraftWebsocketAPI/issues)
- Suggest features
- Submit pull requests
- Improve documentation

---

## Links

<p align="center">
  <a href="https://github.com/addavriance/mcwebapi">
    <img src="https://img.shields.io/badge/Python_Client-mcwebapi-blue?style=for-the-badge&logo=python&logoColor=white" alt="Python Client"/>
  </a>
  <a href="https://pypi.org/project/mcwebapi/">
    <img src="https://img.shields.io/badge/PyPI-mcwebapi-blue?style=for-the-badge&logo=pypi&logoColor=white" alt="PyPI"/>
  </a>
  <a href="https://github.com/addavriance/MinecraftWebsocketAPI/wiki">
    <img src="https://img.shields.io/badge/Wiki-Documentation-green?style=for-the-badge&logo=github" alt="Wiki"/>
  </a>
</p>

---

<div align="center">
  
  **Built with** [NeoForge](https://neoforged.net/) • [Jackson](https://github.com/FasterXML/jackson) • [Netty](https://netty.io/)
  
  <sub>Made with ❤️ by <a href="https://github.com/addavriance">addavriance</a></sub>
  
</div>
