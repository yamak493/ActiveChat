package net.enabify.activeChat.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * チャットイベントごとのポイント獲得条件を管理するクラス
 */
public class PointsManager {
    // 各プレイヤーの最後のチャット時刻（通常チャット用）
    private final Map<UUID, Long> lastChatTime = new ConcurrentHashMap<>();
    
    // 各プレイヤーの最後の歓迎チャット時刻
    private final Map<UUID, Long> lastWelcomeTime = new ConcurrentHashMap<>();
    
    // 各プレイヤーの最後の挨拶チャット時刻
    private final Map<UUID, Long> lastGreetingTime = new ConcurrentHashMap<>();
    
    // 新規プレイヤーが参加した時刻
    private long lastNewPlayerJoinTime = 0;
    
    // 誰かしらのプレイヤーが参加した時刻
    private long lastAnyPlayerJoinTime = 0;
    
    /**
     * 通常チャットのポイント判定
     * @param uuid プレイヤーUUID
     * @param messageLength メッセージの長さ
     * @return 付与するポイント数（条件を満たさない場合は0）
     */
    public int checkNormalChat(UUID uuid, int messageLength) {
        long currentTime = System.currentTimeMillis();
        Long lastTime = lastChatTime.get(uuid);
        
        // 文字数が6文字以下の場合はカウントしない
        if (messageLength <= 6) {
            return 0;
        }
        
        // 間隔が3秒未満の場合はカウントしない
        if (lastTime != null && (currentTime - lastTime) < 3000) {
            return 0;
        }
        
        // 最後のチャット時刻を更新
        lastChatTime.put(uuid, currentTime);
        return 1;
    }
    
    /**
     * 新規さん歓迎チャットのポイント判定
     * @param uuid プレイヤーUUID
     * @param message チャットメッセージ
     * @return 付与するポイント数（条件を満たさない場合は0）
     */
    public int checkWelcomeChat(UUID uuid, String message) {
        long currentTime = System.currentTimeMillis();
        
        // 新規さんが参加してから60秒以内でない場合はカウントしない
        if (currentTime - lastNewPlayerJoinTime > 60000) {
            return 0;
        }
        
        // キーワードチェック
        String lowerMessage = message.toLowerCase();
        boolean isWelcomeMessage = message.contains("082") ||
                                   lowerMessage.contains("初") ||
                                   lowerMessage.contains("新規さん") ||
                                   lowerMessage.contains("よろしく");
        
        if (!isWelcomeMessage) {
            return 0;
        }
        
        // 間隔が30秒未満の場合はカウントしない
        Long lastTime = lastWelcomeTime.get(uuid);
        if (lastTime != null && (currentTime - lastTime) < 30000) {
            return 0;
        }
        
        // 最後の歓迎チャット時刻を更新
        lastWelcomeTime.put(uuid, currentTime);
        return 50;
    }
    
    /**
     * 挨拶チャットのポイント判定
     * @param uuid プレイヤーUUID
     * @param message チャットメッセージ
     * @return 付与するポイント数（条件を満たさない場合は0）
     */
    public int checkGreetingChat(UUID uuid, String message) {
        long currentTime = System.currentTimeMillis();
        
        // プレイヤーが参加してから60秒以内でない場合はカウントしない
        if (currentTime - lastAnyPlayerJoinTime > 60000) {
            return 0;
        }
        
        // キーワードチェック
        String lowerMessage = message.toLowerCase();
        boolean isGreetingMessage = message.contains("08") ||
                                    message.contains("52") ||
                                    message.contains("58") ||
                                    lowerMessage.contains("oha") ||
                                    lowerMessage.contains("おは") ||
                                    lowerMessage.contains("kon") ||
                                    lowerMessage.contains("こん");
        
        if (!isGreetingMessage) {
            return 0;
        }
        
        // 間隔が30秒未満の場合はカウントしない
        Long lastTime = lastGreetingTime.get(uuid);
        if (lastTime != null && (currentTime - lastTime) < 30000) {
            return 0;
        }
        
        // 最後の挨拶チャット時刻を更新
        lastGreetingTime.put(uuid, currentTime);
        return 5;
    }
    
    /**
     * 新規プレイヤーが参加したことを記録
     */
    public void recordNewPlayerJoin() {
        lastNewPlayerJoinTime = System.currentTimeMillis();
    }
    
    /**
     * プレイヤーが参加したことを記録
     */
    public void recordPlayerJoin() {
        lastAnyPlayerJoinTime = System.currentTimeMillis();
    }
}
