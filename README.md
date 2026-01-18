# Jamming Minecraft Stream (Overview)

This repository provides a system to **integrate TikTok Live events into a Minecraft server**.

- **Python side**: A bridge that retrieves TikTok Live events and forwards them to the Minecraft plugin via HTTP.
- **Minecraft plugin side**: Receives events via HTTP and uses them for in-game notifications or command execution.

---

## Directory Structure (Overview)

```
jamming-minecraft-stream/
├─ python/ ← Python bridge
│ ├─ README.md ← English documentation for Python
│ ├─ README_JP.md ← Japanese documentation for Python
│ └─ (code & configuration)
├─ plugins/ ← Minecraft plugin
│ ├─ README.md ← Documentation for the plugin (English)
│ └─ README_JP.md ← Documentation for the plugin (Japanese)
└─ README.md ← This overview
```
---

## Python Bridge Description

The Python bridge retrieves TikTok Live events and sends them to the Minecraft plugin via HTTP.  
For more details, please refer to:

- `python/README.md` (English)
- `python/README_JP.md` (Japanese)

---

## Minecraft Plugin Description

The Minecraft plugin receives HTTP events sent from the Python bridge and uses them for in-game notifications or command execution.

Detailed documentation for the plugin will be created here:

- `plugins/README.md` (English)
- `plugins/README_JP.md` (Japanese)