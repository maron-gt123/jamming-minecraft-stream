# Jamming Minecraft Stream (Overview)

[日本語](https://github.com/maron-gt123/jamming-minecraft-stream/blob/main/README_JP.md)<br>
This repository provides a system to **integrate TikTok Live events into a Minecraft server**.

- **Python side**: A bridge that retrieves TikTok Live events and forwards them to the Minecraft plugin via HTTP.
- **Minecraft plugin side**: Receives events via HTTP and uses them for in-game notifications or command execution.

---

## Directory Structure (Overview)

```
jamming-minecraft-stream/
├─ python/ ← Python bridge
│ ├─ README.md         ← documentation for Python (English)
│ ├─ README_JP.md      ← documentation for Python (Japanese)
│ └─ (code & configuration)
├─ configcreate/       ← Config generator & event trigger reference tool
│ ├─ setup-web/        ← Config generator
│ │  ├─ README.md      ← Tool documentation (English)
│ │  └─ README_JP.md   ← Tool documentation (Japanese)
│ ├─ config-overlay/   ← event trigger reference tool
│ │  ├─ README.md      ← Tool documentation (English)
│ │  └─ README_JP.md   ← Tool documentation (Japanese)
│ └─ gift&command/     ← Gifts and Commands Guide
│    ├─ README.md      ← documentation (English)
│    └─ README_JP.md   ← documentation (Japanese)
└─ README.md        ← This overview
```
---

## Python Bridge Description

The Python bridge retrieves TikTok Live events and sends them to the Minecraft plugin via HTTP.  
For more details, please refer to:

- `python/README.md` (English)
- `python/README_JP.md` (Japanese)

---

## Config Generator & Event Trigger Reference Tool

This tool helps generate the plugin's configuration files and create a reference for event triggers used during streaming.

Detailed documentation will be available at:

- `plugins/README.md` (English)
- `plugins/README_JP.md` (Japanese)