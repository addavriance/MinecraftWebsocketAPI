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

  <p align="center">
    <a href="https://github.com/addavriance/mcwebapi">
      <img src="https://img.shields.io/badge/Python_Client-mcwebapi-blue?style=for-the-badge&logo=github&logoColor=white" alt="Python Client"/>
    </a>
    <a href="https://pypi.org/project/mcwebapi/">
      <img src="https://img.shields.io/badge/PyPI-mcwebapi-blue?style=for-the-badge&logo=pypi&logoColor=white" alt="PyPI"/>
    </a>
    <a href="https://github.com/addavriance/MinecraftWebsocketAPI/wiki">
      <img src="https://img.shields.io/badge/Wiki-Documentation-green?style=for-the-badge&logo=github" alt="Wiki"/>
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

A **client/server-side NeoForge mod** that exposes Minecraft's functionality through a **WebSocket API**, enabling real-time control from external applications in **any programming language**.

```python
# It's THIS simple:
import asyncio
from mcwebapi import MinecraftAPI

async def main():
    async with MinecraftAPI() as api:
        player = api.Player("Steve")
        await player.teleport(0, 100, 0)

        level = api.Level("minecraft:overworld")
        await level.setDayTime(6000)

        block = api.Block("minecraft:overworld")
        await block.setBlock(10, 64, 10, "minecraft:diamond_block")

asyncio.run(main())
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
- **Async/await** native asyncio support
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
import asyncio
from mcwebapi import MinecraftAPI

async def main():
    # Connect to your server
    async with MinecraftAPI(
        host="localhost",
        port=8765,
        auth_key="your-secret-key-here"
    ) as api:
        # Control players
        player = api.Player("Steve")
        await player.setHealth(20.0)
        await player.teleport(0, 100, 0)
        await player.sendMessage("Hello from Python!")

        # Manipulate world
        level = api.Level("minecraft:overworld")
        await level.setDayTime(6000)  # Noon
        await level.setWeather(True, False)  # Rain, no thunder

        # Interact with blocks
        block = api.Block("minecraft:overworld")
        await block.setBlock(10, 64, 10, "minecraft:diamond_block")

asyncio.run(main())
```

### Concurrent Operations

```python
import asyncio
from mcwebapi import MinecraftAPI

async def main():
    async with MinecraftAPI(auth_key="your-key") as api:
        # Get multiple values concurrently
        player1 = api.Player("Steve")
        player2 = api.Player("Alex")

        health1, health2 = await asyncio.gather(
            player1.getHealth(),
            player2.getHealth()
        )

        print(f"Steve: {health1}, Alex: {health2}")

asyncio.run(main())
```

---

## API Modules

The Python client provides comprehensive access to all Minecraft server functionality:

### 🎮 [Player Operations](https://github.com/addavriance/mcwebapi/wiki/Player-Operations)
Health, inventory, teleportation, effects, experience, game mode

### 🌍 [Level Management](https://github.com/addavriance/mcwebapi/wiki/Level-Management)
Time, weather, blocks, world border, spawn point, chunks

### 🧱 [Block Operations](https://github.com/addavriance/mcwebapi/wiki/Block-Operations)
Place/break blocks, manage container inventories (chests, furnaces)

### 👾 [Entity Management](https://github.com/addavriance/mcwebapi/wiki/Entity-Management)
Spawn, remove, teleport, and customize entities

### 🏆 [Scoreboard](https://github.com/addavriance/mcwebapi/wiki/Scoreboard)
Objectives, teams, scores, display slots

### ⚙️ [Server Management](https://github.com/addavriance/mcwebapi/wiki/Server-Management)
Server info, monitoring, player management, commands

**[📖 View Full API Documentation →](https://github.com/addavriance/mcwebapi/wiki)**

---

## Documentation

- **[Python Client Documentation](https://github.com/addavriance/mcwebapi/wiki)** - Complete API reference with examples
- **[Python Client GitHub](https://github.com/addavriance/mcwebapi)** - Source code and issues
- **[PyPI Package](https://pypi.org/project/mcwebapi/)** - Installation and releases

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

## Contributing

Contributions are welcome! Please feel free to:

- Report bugs via [Issues](https://github.com/addavriance/MinecraftWebsocketAPI/issues)
- Suggest features
- Submit pull requests
- Improve documentation
