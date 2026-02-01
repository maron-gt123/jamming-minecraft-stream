import time
import yaml
import re
import signal
from datetime import datetime

import requests
from TikTokLive import TikTokLiveClient
from TikTokLive.events import GiftEvent, LikeEvent, FollowEvent, ShareEvent, SubscribeEvent, ConnectEvent
from TikTokLive.client.errors import UserOfflineError

# =====================
# Loading settings
# =====================
with open("config/config.yml", "r", encoding="utf-8") as f:
    config = yaml.safe_load(f)

USERNAME = config["tiktok"]["user"]
HTTP_CONF = config["http"]
RECONNECT_WAIT = 30
LIKE_RESET_INTERVAL = config.get("reset_interval_seconds", 60 * 60 * 24)
like_user_total = {}

# =====================
# Anti-spam / Rate limiting
# =====================
# event cooldown[sec]
FOLLOW_COOLDOWN = 120
SHARE_COOLDOWN = 120
SUBSCRIBE_COOLDOWN = 120

# last event time
last_follow_time = {}
last_share_time = {}
last_subscribe_time = {}

print(f"[INFO] LIKE_RESET_INTERVAL = {LIKE_RESET_INTERVAL}")

# =====================
# Utilities
# =====================
def clean_nickname(nick: str) -> str:
    nick = re.sub(r'[\U00010000-\U0001FFFF]', '', nick)
    nick = re.sub(r'[\u2000-\u2FFF]', '', nick)
    return nick.strip()


def forward_event(event_type: str, data: dict):
    try:
        payload = {
            "event_type": event_type,
            "timestamp": datetime.now().isoformat(),
            "data": data,
        }

        res = requests.post(
            HTTP_CONF["endpoint"],
            json=payload,
            timeout=HTTP_CONF.get("timeout", 3),
        )

        print(
            f"[{datetime.now()}] [HTTP] {event_type} forwarded "
            f"status={res.status_code}"
        )

    except Exception as e:
        print(f"[{datetime.now()}] [HTTP ERROR] {event_type}: {e}")


# =====================
# TikTok Client
# =====================
def create_client():
    client = TikTokLiveClient(unique_id=USERNAME)

    @client.on(ConnectEvent)
    async def on_connect(event: ConnectEvent):
        print(f"[{datetime.now()}] [INFO] TikTok Live connected")

    # ---- Gift ----
    @client.on(GiftEvent)
    async def on_gift(event: GiftEvent):
            gift = getattr(event, "m_gift", None)
            if not gift:
                return
            streakable = getattr(gift, "combo", False)
            if streakable and event.repeat_end != 1:
                return

            data = {
                "user": event.user.unique_id,
                "nickname": clean_nickname(event.user.nickname),
                "gift_name": gift.name,
                "count": event.repeat_count,
                "streakable": streakable,
                "repeat_end": event.repeat_end,
            }

            print(f"[{datetime.now()}] [GIFT] {data}")
            forward_event("gift", data)

    # ---- Like ----
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
            }

        user_data = like_user_total[nickname]

        # ---- reset if interval passed ----
        if now - user_data["last_reset"] >= LIKE_RESET_INTERVAL:
            print(f"[{datetime.now()}] [LIKE RESET] {nickname}")
            user_data["total"] = 0
            user_data["last_reset"] = now

        # ---- accumulate ----
        prev_total = user_data["total"]
        user_data["total"] += count
        user_total = user_data["total"]

        data = {
            "user": event.user.unique_id,
            "nickname": nickname,
            "count": count,
            "prev_total": prev_total,
            "user_total": user_total,
            "count_total": event.total,
        }

        print(f"[{datetime.now()}] [LIKE] {data}")
        forward_event("like", data)

    # ---- Follow ----
    @client.on(FollowEvent)
    async def on_follow(event: FollowEvent):
        uid = event.user.unique_id
        now = time.time()
        if uid in last_follow_time and now - last_follow_time[uid] < FOLLOW_COOLDOWN:
            print(f"[{datetime.now()}] [FOLLOW SPAM BLOCKED] {uid}")
            return
        last_follow_time[uid] = now

        data = {
            "user": event.user.unique_id,
            "nickname": clean_nickname(event.user.nickname),
        }

        print(f"[{datetime.now()}] [FOLLOW] {data}")
        forward_event("follow", data)

    # ---- Share ----
    @client.on(ShareEvent)
    async def on_share(event: ShareEvent):
        uid = event.user.unique_id
        now = time.time()
        if uid in last_share_time and now - last_share_time[uid] < SHARE_COOLDOWN:
            print(f"[{datetime.now()}] [SHARE SPAM BLOCKED] {uid}")
            return
        last_share_time[uid] = now

        data = {
            "user": uid,
            "nickname": clean_nickname(event.user.nickname),
        }

        print(f"[{datetime.now()}] [SHARE] {data}")
        forward_event("share", data)

    # ---- Subscribe ----
    @client.on(SubscribeEvent)
    async def on_subscribe(event: SubscribeEvent):
        uid = event.user.unique_id
        now = time.time()
        if uid in last_subscribe_time and now - last_subscribe_time[uid] < SUBSCRIBE_COOLDOWN:
            print(f"[{datetime.now()}] [SUBSCRIBE SPAM BLOCKED] {uid}")
            return
        last_subscribe_time[uid] = now

        data = {
            "user": uid,
            "nickname": clean_nickname(event.user.nickname),
        }

        print(f"[{datetime.now()}] [SUBSCRIBE] {data}")
        forward_event("subscribe", data)

    return client


# =====================
# Main Loop
# =====================
while True:
    try:
        print(f"[{datetime.now()}] [INFO] Starting TikTok client")
        client = create_client()
        client.run()

        print(f"[{datetime.now()}] [WARN] client.run() exited, restarting")
        time.sleep(10)

    except UserOfflineError:
        print(
            f"[{datetime.now()}] [INFO] Stream offline, retry in {RECONNECT_WAIT}s"
        )
        time.sleep(RECONNECT_WAIT)

    except KeyboardInterrupt:
        print("Shutdown requested (KeyboardInterrupt)")
        shutdown_event.set()

    except Exception as e:
        print(f"[{datetime.now()}] [ERROR] TikTok client crashed: {e}")
        time.sleep(10)