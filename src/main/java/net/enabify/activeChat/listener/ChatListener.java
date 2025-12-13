package net.enabify.activeChat.listener;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.enabify.activeChat.ActiveChat;
import net.enabify.activeChat.data.PlayerDataManager;
import net.enabify.activeChat.manager.PointsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

/**
 * チャットイベントを監視してポイントを付与するリスナー
 * Folia対応のため非同期処理を使用
 */
public class ChatListener implements Listener {
    private final ActiveChat plugin;
    private final PlayerDataManager dataManager;
    private final PointsManager pointsManager;
    
    public ChatListener(ActiveChat plugin, PlayerDataManager dataManager, PointsManager pointsManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.pointsManager = pointsManager;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String message = event.getMessage();
        int messageLength = message.length();
        
        // 各ポイント判定を実行
        int totalPoints = 0;
        
        // 1. 通常チャットのポイント判定
        int normalPoints = pointsManager.checkNormalChat(uuid, messageLength);
        totalPoints += normalPoints;
        
        // 2. 新規さん歓迎チャットのポイント判定
        int welcomePoints = pointsManager.checkWelcomeChat(uuid, message);
        totalPoints += welcomePoints;
        
        // 3. 挨拶チャットのポイント判定
        int greetingPoints = pointsManager.checkGreetingChat(uuid, message);
        totalPoints += greetingPoints;
        
        // ポイントがある場合は加算
        if (totalPoints > 0) {
            final int points = totalPoints;
            // 非同期でデータを更新
            plugin.getServer().getAsyncScheduler().runNow(plugin, scheduledTask -> {
                dataManager.addPoints(uuid, points);
                dataManager.saveData();
            });
        }
    }
}
