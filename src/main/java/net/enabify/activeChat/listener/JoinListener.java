package net.enabify.activeChat.listener;

import net.enabify.activeChat.ActiveChat;
import net.enabify.activeChat.data.PlayerDataManager;
import net.enabify.activeChat.data.PlayerPoints;
import net.enabify.activeChat.manager.PointsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * プレイヤーの参加/退出イベントを監視するリスナー
 * Folia対応のため非同期処理を使用
 */
public class JoinListener implements Listener {
    private final ActiveChat plugin;
    private final PlayerDataManager dataManager;
    private final PointsManager pointsManager;
    
    // 直近5分以内に退出したプレイヤーを追跡
    private final Map<UUID, Long> recentQuits = new ConcurrentHashMap<>();
    
    public JoinListener(ActiveChat plugin, PlayerDataManager dataManager, PointsManager pointsManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.pointsManager = pointsManager;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // 新規プレイヤーかどうかを判定
        if (!player.hasPlayedBefore()) {
            pointsManager.recordNewPlayerJoin();
        }
        
        // 直近5分以内に退出したプレイヤーを除外して判定
        boolean isRecentRejoin = false;
        Long quitTime = recentQuits.get(uuid);
        if (quitTime != null) {
            long timeSinceQuit = System.currentTimeMillis() - quitTime;
            if (timeSinceQuit < TimeUnit.MINUTES.toMillis(5)) {
                isRecentRejoin = true;
            } else {
                recentQuits.remove(uuid);
            }
        }
        
        // 直近5分以内に退出したプレイヤーでない場合は、プレイヤー参加として記録
        if (!isRecentRejoin) {
            pointsManager.recordPlayerJoin();
        }
        
        // 前回獲得したポイントを付与
        plugin.getServer().getAsyncScheduler().runDelayed(plugin, scheduledTask -> {
            PlayerPoints points = dataManager.getAndResetPoints(uuid);
            
            if (points.getTotal() > 0) {
                // コマンド実行はグローバルスケジューラーで実行（Folia対応）
                Bukkit.getServer().getGlobalRegionScheduler().run(plugin, task -> {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                        "points give " + player.getName() + " " + points.getTotal());
                });
                
                // ポイント内訳メッセージはプレイヤースケジューラーで実行
                player.getScheduler().run(plugin, task -> {
                    player.sendMessage(ChatColor.GREEN + "たくさんチャットと挨拶をしてポイントを貯めよう！\n前回のログインで獲得したポイント：\n" + points.getDetails());
                }, null);
            }
            
            // データを保存
            dataManager.saveData();
        }, 20L, TimeUnit.MILLISECONDS); // 1秒後に実行
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // 退出時刻を記録
        recentQuits.put(uuid, System.currentTimeMillis());
        
        // 古い退出記録をクリーンアップ（10分以上前のもの）
        plugin.getServer().getAsyncScheduler().runNow(plugin, scheduledTask -> {
            long currentTime = System.currentTimeMillis();
            recentQuits.entrySet().removeIf(entry -> 
                currentTime - entry.getValue() > TimeUnit.MINUTES.toMillis(10));
        });
    }
}
