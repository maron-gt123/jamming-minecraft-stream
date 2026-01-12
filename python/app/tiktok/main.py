import time
import yaml
import re
import queue
import threading
from datetime import datetime

from mcrcon import MCRcon
from TikTokLive import TikTokLiveClient
from TikTokLive.events import GiftEvent, LikeEvent, FollowEvent, ShareEvent, SubscribeEvent, ConnectEvent
from TikTokLive.client.errors import UserOfflineError

# =====================
# Loading settings
# =====================
with open("config/config.yml", "r", encoding="utf-8") as f:
    config = yaml.safe_load(f)

USERNAME = config["tiktok"]["user"]
RCON_CONF = config["minecraft"]
RULES = config["rules"]

LIKE_RESET_INTERVAL = config.get("reset_interval_seconds", 60 * 60 * 24)
like_user_total = {}
# {
#   nickname: {
#       "total": int,
#       "last_reset": float (timestamp)
#   }
# }

print(f"[INFO] LIKE_RESET_INTERVAL = {LIKE_RESET_INTERVAL}")

# =====================
# RCON setup
# =====================
rcon_queue = queue.Queue()
rcon = None

def connect_rcon():
    global rcon
    try:
        rcon = MCRcon(RCON_CONF["RCON_HOST"], RCON_CONF["RCON_PASSWORD"], port=RCON_CONF["RCON_PORT"])
        rcon.connect()
        print(f"[{datetime.now()}] [RCON] Connected")
    except Exception as e:
        rcon = None
        print(f"[{datetime.now()}] [RCON ERROR] Failed to connect: {e}")

def send_rcon(command: str):
    rcon_queue.put(command)

def rcon_worker():
    while True:
        cmd = rcon_queue.get()
        try:
            if rcon is None:
                print(f"[{datetime.now()}] [RCON ERROR] Not connected, skipping command: {cmd}")
            else:
                print(f"[{datetime.now()}] [RCON] {cmd}")
                rcon.command(cmd)
        except Exception as e:
            print(f"[{datetime.now()}] [RCON ERROR] {e} (command: {cmd})")
        finally:
            rcon_queue.task_done()

threading.Thread(target=rcon_worker, daemon=True).start()

# =====================
# Common utilities
# =====================
def clean_nickname(nick: str) -> str:
    # Remove emojis and special Unicode characters
    nick = re.sub(r'[\U00010000-\U0001FFFF]', '', nick)
    nick = re.sub(r'[\u2000-\u2FFF]', '', nick)
    return nick.strip()

def run_actions(actions: list, event_type="RCON"):
    for act in actions:
        try:
            times = int(act.get("times", 1))
            for _ in range(times):
                send_rcon(act["command"])
                print(f"[{datetime.now()}] [{event_type}] Scheduled: {act['command']}")
        except Exception as e:
            print(f"[{datetime.now()}] [ERROR] run_actions failed: {e} | action: {act}")

# =====================
# Rule evaluation
# =====================
def handle_event(event_type: str, data: dict):
    try:
        for rule in RULES:
            if rule["type"] != event_type:
                continue

            # ---- gift ----
            if event_type == "gift":
                if rule.get("gift_name") != data["gift_name"]:
                    continue

                gift_name = data["gift_name"]
                count = data["count"]
                repeat_end = data["repeat_end"]
                # ---- Heart Me : instant execution ----
                if gift_name == "Heart Me":
                    pass

                # ---- other gifts : repeat_end only ----
                else:
                    if repeat_end != 1:
                        continue

                    rule_count = rule.get("count")
                    if rule_count is None:
                        continue

                    if count < rule_count:
                        continue

            # ---- like ----
            if event_type == "like":
                rule_count = rule.get("count")
                nickname = data["nickname"]
                prev_total = data["prev_total"]
                user_total = data["user_total"]

                # ---- Arrival point check ----
                if not (prev_total < rule_count <= user_total):
                    continue
                user_state = like_user_total.get(nickname)
                if user_state is None:
                    continue
                # ---- Double execution prevention ----
                if rule_count in user_state["fired_counts"]:
                    continue
                user_state["fired_counts"].add(rule_count)
            run_actions(rule.get("actions", []), event_type)

    except Exception as e:
        print(f"[{datetime.now()}] [ERROR] handle_event failed: {e} | data: {data}")

# =====================
# Events
# =====================
def create_client():
    client = TikTokLiveClient(unique_id=USERNAME)


    @client.on(ConnectEvent)
    async def on_connect(event: ConnectEvent):
        print(f"[{datetime.now()}] [INFO]  TikTok Live Connection complete")

    # ---- gift ----
    @client.on(GiftEvent)
    async def on_gift(event: GiftEvent):
        nickname = clean_nickname(event.user.nickname)
        data = {
            "user": event.user.unique_id,
            "nickname": nickname,
            "gift_name": event.gift.name,
            "count": event.repeat_count,
            "repeat_end": event.repeat_end,
        }
        print(f"[{datetime.now()}] [GIFT] {data}")
        handle_event("gift", data)

    # ---- like ----
    @client.on(LikeEvent)
    async def on_like(event: LikeEvent):
        nickname = clean_nickname(event.user.nickname)
        now = time.time()
        count = event.count
        # ---- initialize user ----
        if nickname not in like_user_total:
            like_user_total[nickname] = {
                "total": 0,
                "last_reset": now,
                "fired_counts": set()
            }

        user_data = like_user_total[nickname]

        # ---- reset if interval passed ----
        if now - user_data["last_reset"] >= LIKE_RESET_INTERVAL:
            print(f"[{datetime.now()}] [LIKE RESET] {nickname}")
            user_data["total"] = 0
            user_data["last_reset"] = now
            user_data["fired_counts"].clear()

        # ---- accumulate ----
        prev_total = user_data["total"]
        user_data["total"] += count
        user_total = user_data["total"]

        data = {
            "user": event.user.unique_id,
            "nickname": nickname,
            "count": count,
            "user_total": user_total,
            "prev_total": prev_total,
            "count_total": event.total,
        }

        print(f"[{datetime.now()}] [LIKE] {data}")
        handle_event("like", data)

    # ---- follow ----
    @client.on(FollowEvent)
    async def on_follow(event: FollowEvent):
        nickname = clean_nickname(event.user.nickname)
        data = {
            "user": event.user.unique_id,
            "nickname": nickname,
        }
        print(f"[{datetime.now()}] [FOLLOW] {data}")
        handle_event("follow", data)

    # ---- share ----
    @client.on(ShareEvent)
    async def on_share(event: ShareEvent):
        nickname = clean_nickname(event.user.nickname)
        data = {
            "user": event.user.unique_id,
            "nickname": nickname,
        }
        print(f"[{datetime.now()}] [SHARE] {data}")
        handle_event("share", data)

    # ---- subscribe ----
    @client.on(SubscribeEvent)
    async def on_subscribe(event: SubscribeEvent):
        nickname = clean_nickname(event.user.nickname)
        data = {
            "user": event.user.unique_id,
            "nickname": nickname,
        }
        print(f"[{datetime.now()}] [SUBSCRIBE] {data}")
        handle_event("subscribe", data)

    return client
RECONNECT_WAIT = 30

# =====================
# Loop
# =====================
while True:
    try:
        if rcon is None:
            connect_rcon()
        print(f"[{datetime.now()}] [INFO] Starting TikTok client")
        client = create_client()
        client.run()
        print(f"[{datetime.now()}] [WARN] client.run() exited")

    except UserOfflineError:
        print(f"[{datetime.now()}] [INFO] Stream offline, retry in {RECONNECT_WAIT}s")
        try:
            time.sleep(RECONNECT_WAIT)
        except KeyboardInterrupt:
            print("Shutdown requested during reconnect wait")
            break
    except KeyboardInterrupt:
        print("Shutdown requested")
        break
    except Exception as e:
        print(f"[{datetime.now()}] [ERROR] TikTok client crashed: {e}")
        time.sleep(10)