# Jamming Minecraft Stream（全体概要）

このリポジトリは、**TikTok LiveのイベントをMinecraftサーバに連携するための仕組み**を提供します。

- **Python側**：TikTok Liveのイベントを取得し、HTTPでMinecraftプラグインへ転送するブリッジ
- **Minecraftプラグイン側**：HTTPで受信したイベントをゲーム内で通知したり、コマンドを実行する
---

## ディレクトリ構成（概要）

```
jamming-minecraft-stream/
├─ python/ ← Pythonブリッジ
│ ├─ README.md ← Pythonの英語ドキュメント
│ ├─ README_JP.md ← Pythonの日本語ドキュメント
│ └─ (コード・設定)
├─ plugins/ ← Minecraftプラグイン
│ ├─ README.md ← プラグインのドキュメント
│ └─ README_JP.md ← プラグインの日本語ドキュメント
└─ README.md ← この全体概要
```
---

## Pythonブリッジの説明
PythonブリッジはTikTok Liveのイベントを取得し、MinecraftプラグインへHTTP送信します。  
詳細は以下を参照してください。

- `python/README.md`（英語）
- `python/README_JP.md`（日本語）

---

## Minecraftプラグインの説明

MinecraftプラグインはPythonブリッジから送られてくるHTTPイベントを受け取り、  
ゲーム内での通知やコマンド実行などに利用します。

プラグイン側の詳細ドキュメントは以下に作成します。
- `plugins/README.md`（英語）
- `plugins/README_JP.md`（日本語）