# Minecraft Bridge (Python)

## 機能概要
このPythonプログラムは **TikTok Liveのイベントを取得し、Minecraftサーバへ橋渡し（中継）する**ためのツールです。

- TikTok Liveで発生したイベント（いいね、ギフト、フォロー等）を監視
- そのイベント情報をHTTP経由でMinecraftプラグインへ送信
- Minecraft側で受信したイベントをゲーム内通知やコマンド実行に利用可能

つまり **「配信サイト（TikTok） → Python → Minecraft」** の流れを作るブリッジ（橋渡し）役です。

---

## 構成
```bash
project/
├─ main.py
└─ config/
└─ config.yml
```

---

## main.py の役割

### 1. 設定読み込み
`config/config.yml` からTikTokユーザー名やHTTP送信先などを読み込みます。

### 2. TikTok Liveイベント取得
TikTok Liveのイベントを取得し、以下を検知します。

- Gift（ギフト）
- Like（いいね）
- Follow（フォロー）
- Share（シェア）
- Subscribe（登録）
- Connect（接続）

### 3. イベントをHTTPで転送
イベントが発生したら、JSON形式でHTTP POSTを実施しMinecraftプラグインへ送信します。

---

## config.yml の役割

### 1. TikTok設定
```yaml
tiktok:
  user: tv_asahi_news
```
- TikTok配信者のユーザー名（@無し）
### 2. HTTP転送設定
```yaml
http:
  endpoint: "http://localhost:8080/"
  timeout: 5
```
- `endpoint`: Minecraftプラグインが立てたHTTPサーバのURL
- `timeout`: HTTP通信のタイムアウト（秒）

### 3. Like合計リセット設定
```yaml
reset_interval_seconds: 60
```
- ユーザーごとのいいね合計をリセットする間隔（秒）

---

## 実行方法
このプロジェクトは Dockerイメージとして提供されています。
Dockerを使って実行する場合、以下の手順で起動します。

### 1. Dockerイメージを取得
```bash
docker pull marongt123/jamming-minecraft-stream:tiktok
```
### 2. Dockerコンテナ起動
```bash
docker run -d \
  --name jamming-tiktok \
  -v /path/to/config:/app/config \
  marongt123/jamming-minecraft-stream:tiktok
```
- `/path/to/config` は `config.yml` を置いたディレクトリを指定してください
- `config.yml` はコンテナ内の `/app/config/config.yml` にマウントされます

## 注意
- TikTok配信がオフラインの場合は再接続を試行します
- HTTP転送に失敗してもログを出して継続します