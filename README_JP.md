# Jamming Minecraft Stream（全体概要）

[English](https://github.com/maron-gt123/jamming-minecraft-stream/blob/main/README.md)<br>
このリポジトリは、**TikTok LiveのイベントをMinecraftサーバに連携するための仕組み**を提供します。

- **Python側**：TikTok Liveのイベントを取得し、HTTPでMinecraftプラグインへ転送するブリッジ
- **Minecraftプラグイン側**：HTTPで受信したイベントをゲーム内で通知したり、コマンドを実行する
---

## ディレクトリ構成（概要）

```
jamming-minecraft-stream/
├─ python/ ← Pythonブリッジ
│ ├─ README.md         ← Pythonのドキュメント（英語）
│ ├─ README_JP.md      ← Pythonのドキュメント（日本語）
│ └─ （コード・設定）
├─ configcreate/       ← コンフィグ作成ツール & イベントトリガー凡例ツール
│ ├─ setup-web/        ← コンフィグ作成ツール
│ │  ├─ README.md      ← ツールドキュメント（英語）
│ │  └─ README_JP.md   ← ツールドキュメント（日本語）
│ ├─ config-overlay/   ← イベントトリガー凡例ツール
│ │  ├─ README.md      ← ツールドキュメント（英語）
│ │  └─ README_JP.md   ← ツールドキュメント（日本語）
│ └─ gift&command/     ← ギフト＆コマンド解説
│    ├─ README.md      ← ドキュメント（英語）
│    └─ README_JP.md   ← ドキュメント（日本語）
└─ README.md           ← この概要
```
---

## Pythonブリッジの説明
PythonブリッジはTikTok Liveのイベントを取得し、MinecraftプラグインへHTTP送信します。  
詳細は以下を参照してください。

- `python/README.md`（英語）
- `python/README_JP.md`（日本語）

---

## config作成及びイベントトリガー凡例作成ツールの説明

プラグインの設定であるconfigファイルの作成及び配信時のイベントトリガー凡例の作成ツールの解説

詳細ドキュメントは以下に作成します。
- `plugins/README.md`（英語）
- `plugins/README_JP.md`（日本語）