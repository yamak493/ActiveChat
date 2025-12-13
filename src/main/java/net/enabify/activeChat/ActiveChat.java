package net.enabify.activeChat;

import net.enabify.activeChat.data.PlayerDataManager;
import net.enabify.activeChat.listener.ChatListener;
import net.enabify.activeChat.listener.JoinListener;
import net.enabify.activeChat.manager.PointsManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * ActiveChat - チャット活性化プラグイン
 * Folia対応の非同期処理を使用
 */
public final class ActiveChat extends JavaPlugin {
    
    private PlayerDataManager dataManager;
    private PointsManager pointsManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("ActiveChatプラグインを起動しています...");
        
        // データマネージャーの初期化
        dataManager = new PlayerDataManager(getDataFolder());
        getLogger().info("プレイヤーデータを読み込みました。");
        
        // ポイントマネージャーの初期化
        pointsManager = new PointsManager();
        
        // イベントリスナーの登録
        getServer().getPluginManager().registerEvents(
            new ChatListener(this, dataManager, pointsManager), this);
        getServer().getPluginManager().registerEvents(
            new JoinListener(this, dataManager, pointsManager), this);
        
        getLogger().info("ActiveChatプラグインが正常に起動しました！");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("ActiveChatプラグインをシャットダウンしています...");
        
        // データを保存
        if (dataManager != null) {
            dataManager.saveData();
            getLogger().info("プレイヤーデータを保存しました。");
        }
        
        getLogger().info("ActiveChatプラグインが正常にシャットダウンしました。");
    }
    
    public PlayerDataManager getDataManager() {
        return dataManager;
    }
    
    public PointsManager getPointsManager() {
        return pointsManager;
    }
}
