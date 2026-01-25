import threading
import csv
import os
import time
from typing import Set

from TikTokLive.client.client import TikTokLiveClient
from TikTokLive.events import ConnectEvent, GiftEvent
from TikTokLive.client.errors import UserOfflineError

TARGETS = [
    "@user1",
    "@user2",
    "@user3"
]

gift_set: Set[str] = set()
lock = threading.Lock()


def ensure_csv_header():
    if not os.path.exists("gifts.csv"):
        with open("gifts.csv", "w", newline="", encoding="utf-8") as f:
            writer = csv.writer(f)
            writer.writerow([
                "gift_id", "gift_name", "diamond", "image_url", "streakable"
            ])


def load_existing_gifts():
    if not os.path.exists("gifts.csv"):
        return

    with open("gifts.csv", "r", newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            gift_set.add(row["gift_id"])


class Watcher:
    def __init__(self, unique_id: str):
        self.unique_id = unique_id
        self.client = TikTokLiveClient(unique_id=unique_id)
        self.client.on(ConnectEvent)(self.on_connect)
        self.client.on(GiftEvent)(self.on_gift)

    async def on_connect(self, event: ConnectEvent):
        print(f"[{self.unique_id}] Connected!")

    async def on_gift(self, event: GiftEvent):
        gift = getattr(event, "m_gift", None)
        if not gift:
            return

        gift_id = getattr(gift, "id", None)
        gift_name = getattr(gift, "name", None)
        diamond = getattr(gift, "diamond_count", 0)
        streakable = getattr(gift, "combo", False)

        image_url = None
        if getattr(gift, "image", None):
            urls = getattr(gift.image, "m_urls", [])
            if urls:
                image_url = urls[0]

        if not gift_id or not gift_name:
            return

        key = str(gift_id)

        with lock:
            if key not in gift_set:
                gift_set.add(key)
                with open("gifts.csv", "a", newline="", encoding="utf-8") as f:
                    writer = csv.writer(f)
                    writer.writerow([
                        gift_id,
                        gift_name,
                        diamond,
                        image_url,
                        streakable
                    ])

    def run(self):
        while True:
            try:
                self.client.run()
            except UserOfflineError:
                print(f"[{self.unique_id}] オフラインです。10分後に再接続します...")
                time.sleep(600)
            except Exception as e:
                print(f"[{self.unique_id}] エラー: {e}")
                time.sleep(10)


def main():
    ensure_csv_header()
    load_existing_gifts()

    # 重複排除
    watchers = [Watcher(uid) for uid in set(TARGETS)]
    threads = []

    for w in watchers:
        t = threading.Thread(target=w.run, daemon=True)
        t.start()
        threads.append(t)

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("終了")


if __name__ == "__main__":
    main()