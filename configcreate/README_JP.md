# TikTok Stream config.yml Generator

## 概要

このツールは、Minecraftサーバー用の `config.yml` を簡単に生成できるWeb UIです。TikTokのストリームイベントに応じた自動処理設定を作成できます。  
➡ [ツールはこちらから利用可能](https://maron-gt123.github.io/jamming-minecraft-stream/configcreate/setup-web/)<br>
➡ [作成したconfigのプリセット表は以下で作成](https://maron-gt123.github.io/jamming-minecraft-stream/configcreate/config-overlay/)<br>
このツールを使うと以下の設定が可能です：


- **BOX内部の自動変換設定**
    - Minecraftのバージョン選択
    - 上・中・下のブロック選択（検索可）
- **イベント設定**
    - Gift / Like / Follow / Share / Subscribe / Comment の有効/無効
    - イベントごとのコマンド設定（複数追加可）
- **ギフト設定**
    - 連続可能／不可能ギフトの管理
    - ギフトごとのコマンド設定（複数追加可）
- **Like設定**
    - 条件付きルール追加（count指定）
    - ルールごとのコマンド設定
- **YAML生成**
    - 設定内容を元に `config.yml` 形式で出力

---

## 使い方

### 1. Minecraftバージョンを選択

画面上部のセレクトボックスから、使用するMinecraftのバージョンを選択します。  
選択に応じてブロック情報やテクスチャが自動で更新されます。

---

### 2. BOX内部のブロック設定

- **Top / Middle / Bottom** の3箇所を設定可能
- 検索ボックスにブロック名を入力すると、候補リストが絞り込まれます
- プルダウンからブロックを選択すると、右側にブロックの画像が表示されます

---

### 3. イベント設定

各イベント（Gift / Like / Follow / Share / Subscribe / Comment）のチェックボックスで有効/無効を切り替えます。

#### コマンドの追加

1. 「＋ command」ボタンをクリック
2. プルダウンからコマンドを選択
3. 必要に応じてオプション（メッセージや数値、選択肢）を入力
4. 複数コマンドも追加可能

#### コマンド解説
| コマンド | コマンドオプション | 実行内容                  | コマンドオプション解説 |
| ----------------------------: |-----------|-----------------------|---|
| jammingevent text | msg | チャット欄にメッセージを表示します     | テキスト記載で表示 |
| jammingevent title | msg | 画面中央にタイトルを表示します       | テキスト記載で表示 |
| jammingevent tnt | Index | TNTをスポーンします           | スポーン数を指定 |
| jammingevent extnt | Index | 強化なTNTをスポーンします        | スポーン数を指定 |
| jammingevent reset | dragon | エンダードラゴンでBOX内ブロックを全破壊 | - |
| jammingevent reset | wither | ウィザーでBOX内ブロックを全破壊     | - |
| jammingevent fill | none | BOX内をブロックで全埋め         | - |
| jammingevent fillblock | Index | BOX内を指定した高さ分ブロックで埋めます | 埋める高さを指定 |
| jammingevent prison | Index | プレイヤーを閉じ込めます          | 閉じ込める時間を指定 |
| jammingevent addclear | Index | クリア数を追加します            | 増加数指定 |
| jammingevent delclear | Index     | クリア数を減少します            | 減少数指定 |
| jammingevent rocket | Index     | 花火ロケットを発射します          | 発射数指定 |
| jammingevent creeper | Index     | クリーパーをスポーンします         | スポーン数を指定 |
| jammingevent mob chicken | Index     | ニワトリをスポーンします          | スポーン数を指定 |
| jammingevent mob pig | Index     | ブタをスポーンします            | スポーン数を指定 |
| jammingevent mob sheep | Index     | ヒツジをスポーンします           | スポーン数を指定 |
| jammingevent mob salmon | Index     | サーモンをスポーンします          | スポーン数を指定 |
| jammingevent doubleplace time | Index     | 一定時間ブロックを2倍設置         | timeで時間指定 |
| jammingevent heightup | Index     | BOXの高さを増加します          | 上方向に拡張 |
| jammingevent heightdown | Index     | BOXの高さを減少します          | 下方向に縮小 |
| jammingevent sizeup | Index     | BOXのサイズを拡張します         | 横方向に拡張 |
| jammingevent sizedown | Index     | BOXのサイズを縮小します         | 横方向に縮小 |
| jammingevent size_reset | Index     | BOXサイズを初期化します         | 初期値に戻す |

---

### 4. ギフト設定

- **連続不可ギフト**： `ギフト設定（連続不可）` に表示
- **連続可能ギフト**： `ギフト設定（連続可）` に表示
- チェックボックスで有効/無効を切替
- 「＋ command」ボタンでギフトに対応するコマンドを追加可能
- 連続可能ギフトは回数を設定できます

---

### 5. Like設定（count条件）

- 「＋ ルール追加」ボタンで新しいルールを作成
- `count` に条件値を入力
- ルールごとにコマンドを設定可能
- 不要なルールは「削除」ボタンで削除

---

### 6. YAML生成

- 画面下部の「YAML生成」ボタンをクリック
- 設定内容に応じた `config.yml` が生成され、下のテキストエリアに表示されます
- 内容をコピーしてMinecraftサーバーに適用可能

---

## 注意事項

- コマンドやギフト情報は同リポジトリ格納のCSVファイルから取得しています。
- ブロックテクスチャや言語情報はMinecraft公式のアセットリポジトリを参照しています
- YAML生成結果は必ず内容を確認してからサーバーに反映してください

---

## ライセンス

このツールの使用・改変は自由ですが、公式Minecraftアセットや外部CSVの使用条件に従ってください。
