package jp.master.jamming.config;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/*
 * ============================================
 * ConfigManager
 * --------------------------------------------
 * プラグイン全体で使用する
 * config.yml の値を一元管理するクラス
 *
 * このクラスの責務：
 *  ・JavaPlugin の config へのアクセス窓口
 *  ・各機能（http / events / commands / jammingbox）の
 *    設定値取得を集約
 *  ・reload 処理を共通化
 *
 * 設計方針：
 *  ・すべて static で扱い、どこからでも参照可能にする
 *  ・設定の読み取りのみを行い、ロジックは持たない
 * ============================================
 */
public class ConfigManager {

    /** プラグイン本体（config 参照・reload 用） */
    private static JavaPlugin plugin;

    // =========================================================
    // HTTP 設定
    // =========================================================

    /** HTTP サーバーの待ち受けポート */
    private static int port;
    /** HTTP イベント受信パス */
    private static String path;
    /** 最後に処理したニックネーム（状態保持用） */
    private static String lastNickname;

    // =========================================================
    // Config Load
    // =========================================================

    /**
     * config.yml を読み込み、内部キャッシュを初期化する
     * ・プラグイン起動時
     * ・reload 時
     * に呼ばれることを想定
     */
    public static void loadConfig(JavaPlugin pluginInstance) {
        plugin = pluginInstance;

        port = plugin.getConfig().getInt("http.port", 8080);
        path = plugin.getConfig().getString("http.path", "/event");
    }

    // =========================================================
    // HTTP Getter
    // =========================================================

    /** HTTP サーバーのポート番号を取得 */
    public static int getPort() {
        return port;
    }
    /** HTTP イベント受信パスを取得 */
    public static String getPath() {
        return path;
    }
    // =========================================================
    // Events 設定
    // =========================================================
    /**
     * 指定したイベントタイプが有効かどうかを判定する
     * 例：
     * events.follow.enabled
     * events.gift.enabled
     */
    public static boolean isEventEnabled(String eventType) {
        return plugin.getConfig().getBoolean(
                "events." + eventType + ".enabled", false
        );
    }
    // =========================================================
    // Commands 設定
    // =========================================================
    /**
     * 指定したイベントタイプに紐づくコマンド定義を取得する
     * config.yml では List<Map<String, Object>> として定義されている想定
     * ・未定義の場合は空リストを返す
     * ・呼び出し側で中身を解釈する
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getCommands(String eventType) {
        List<?> list = plugin.getConfig().getList("commands." + eventType);
        if (list == null) return Collections.emptyList();
        return (List<Map<String, Object>>) list;
    }
    // =========================================================
    // JammingBox 設定
    // =========================================================
    /** JammingBox のクリア前カウントダウン秒数 */
    public static int getClearCountdown() {
        return plugin.getConfig().getInt("jammingbox.clear.countdown", 15);
    }

    /** JammingBox 内部置換機能のデフォルト有効状態を取得 */
    public static boolean getReplaceEnabledDefault() {
        return plugin.getConfig().getBoolean(
                "jammingbox.replace.enabled",
                false
        );
    }

    /** 現在の内部置換機能の有効状態を取得 */
    public static boolean isReplaceEnabled() {
        return plugin.getConfig().getBoolean("jammingbox.replace.enabled", false);
    }
    /** 内部に使用するブロック名を取得 */
    public static String getReplaceBottom() {
        return plugin.getConfig().getString("jammingbox.replace.bottom");
    }
    public static String getReplaceMiddle() {
        return plugin.getConfig().getString("jammingbox.replace.middle");
    }
    public static String getReplaceTop() {
        return plugin.getConfig().getString("jammingbox.replace.top");
    }
    // =========================================================
    // Reload / Runtime State
    // =========================================================
    /**
     * config.yml を再読み込みする
     * ・plugin.reloadConfig()
     * ・内部キャッシュ再初期化
     */
    public static void reload() {
        plugin.reloadConfig();
        loadConfig(plugin);
    }
    /** 最後に処理したニックネームを保存する */
    public static void setLastNickname(String nickname) {
        lastNickname = nickname;
    }
    /** 最後に処理したニックネームを取得する */
    public static String getLastNickname() {
        return lastNickname;
    }
}
