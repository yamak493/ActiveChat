package net.enabify.activeChat.listener;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.enabify.activeChat.ActiveChat;
import net.enabify.activeChat.data.PlayerDataManager;
import net.enabify.activeChat.manager.PointsManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * チャットイベントを監視してポイントを付与するリスナー
 * Folia対応のため非同期処理を使用
 */
public class ChatListener implements Listener {
    private final ActiveChat plugin;
    private final PlayerDataManager dataManager;
    private final PointsManager pointsManager;
    
    // スパム検知用：プレイヤーの前回メッセージ
    private final Map<UUID, String> lastMessage = new ConcurrentHashMap<>();
    // スパム検知用：同じメッセージの連続送信回数
    private final Map<UUID, Integer> spamCount = new ConcurrentHashMap<>();
    
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
        
        // スパム検知
        checkSpam(player, message);
        
        // 各ポイント判定を実行
        int normalPoints = 0;
        int greetingPoints = 0;
        int welcomePoints = 0;
        
        // 1. 通常チャットのポイント判定
        normalPoints = pointsManager.checkNormalChat(uuid, messageLength);
        
        // 2. 新規さん歓迎チャットのポイント判定
        welcomePoints = pointsManager.checkWelcomeChat(uuid, message);
        
        // 3. 挨拶チャットのポイント判定
        greetingPoints = pointsManager.checkGreetingChat(uuid, message);
        
        // ポイントを分類して記録
        if (normalPoints > 0 || greetingPoints > 0 || welcomePoints > 0) {
            int finalNormalPoints = normalPoints;
            int finalGreetingPoints = greetingPoints;
            int finalWelcomePoints = welcomePoints;
            plugin.getServer().getAsyncScheduler().runNow(plugin, scheduledTask -> {
                if (finalNormalPoints > 0) {
                    dataManager.addNormalChatPoints(uuid, finalNormalPoints);
                }
                if (finalGreetingPoints > 0) {
                    dataManager.addGreetingPoints(uuid, finalGreetingPoints);
                }
                if (finalWelcomePoints > 0) {
                    dataManager.addWelcomeNewPlayerPoints(uuid, finalWelcomePoints);
                }
                dataManager.saveData();
            });
        }
    }
    
    /**
     * スパム検知：同じメッセージを3回連続で送信したかチェック
     */
    private void checkSpam(Player player, String message) {
        UUID uuid = player.getUniqueId();
        String currentLast = lastMessage.get(uuid);
        int currentCount = spamCount.getOrDefault(uuid, 0);
        
        if (message.equals(currentLast)) {
            // 前回と同じメッセージ
            currentCount++;
            spamCount.put(uuid, currentCount);
            
            // 3回連続で同じメッセージを送信した場合はミュート
            if (currentCount >= 3) {
                final String playerName = player.getName();
                // グローバルスケジューラーでコマンド実行
                Bukkit.getServer().getGlobalRegionScheduler().run(plugin, task -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                        "mute " + playerName + " 5m スパムを検知しました");
                });
                
                // スパムカウントをリセット
                spamCount.remove(uuid);
                lastMessage.remove(uuid);
            }
        } else {
            // 異なるメッセージが送信された
            lastMessage.put(uuid, message);
            spamCount.put(uuid, 1);
        }
    }
}
