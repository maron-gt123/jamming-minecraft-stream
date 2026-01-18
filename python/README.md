# Minecraft Bridge (Python)

## Overview
This Python program is a tool that **retrieves TikTok Live events and forwards them to a Minecraft server**.

- Monitors events occurring on TikTok Live (likes, gifts, follows, etc.)
- Sends event data to a Minecraft plugin via HTTP
- The Minecraft plugin can use the received events for in-game notifications or command execution

In other words, it acts as a bridge for the flow **“Streaming platform (TikTok) → Python → Minecraft”**.

---

## Structure
```bash
project/
├─ main.py
└─ config/
   └─ config.yml
```

---

## Role of main.py

### 1. Load configuration
Reads the TikTok username and HTTP destination from`config/config.yml` .

### 2. Retrieve TikTok Live events
Listens for TikTok Live events and detects the following:

- Gift
- Like
- Follow
- Share
- Subscribe
- Connect

### 3. Forward events via HTTP
When an event occurs, it sends a JSON payload via HTTP POST to the Minecraft plugin.

---

## Role of config.yml

### 1. TikTok settings
```yaml
tiktok:
  user: tv_asahi_news
```
- TikTok streamer username (without the @)
### HTTP forwarding settings
```yaml
http:
  endpoint: "http://localhost:8080/"
  timeout: 5
```
- `endpoint`: URL of the HTTP server hosted by the Minecraft plugin
- `timeout`: HTTP request timeout (seconds)

### 3. Like reset settings
```yaml
reset_interval_seconds: 60
```
- Interval (seconds) to reset per-user like totals

---

## How to Run
This project is provided as a Docker image.
To run it using Docker, follow these steps:

### 1. Pull the Docker image
```bash
docker pull marongt123/jamming-minecraft-stream:tiktok
```
### 2. Start the Docker container
```bash
docker run -d \
  --name jamming-tiktok \
  -v /path/to/config:/app/config \
  marongt123/jamming-minecraft-stream:tiktok
```
- Replace`/path/to/config` with the directory containing `config.yml`
- `config.yml`will be mounted into the container at`/app/config/config.yml`

## Notes
- If the TikTok stream is offline, the program will attempt to reconnect.
- If HTTP forwarding fails, it will log the error and continue running.