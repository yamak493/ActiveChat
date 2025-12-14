package net.enabify.activeChat.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * プレイヤーのポイントデータを管理するクラス
 */
public class PlayerDataManager {
    private final File dataFile;
    private final Gson gson;
    private final Map<UUID, PlayerPoints> playerPoints;
    
    public PlayerDataManager(File dataFolder) {
        this.dataFile = new File(dataFolder, "playerPoints.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.playerPoints = new ConcurrentHashMap<>();
        loadData();
    }
    
    /**
     * JSONファイルからデータを読み込む
     */
    private void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
                saveData(); // 空のJSONファイルを作成
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        
        try (FileReader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Map<String, PlayerPoints>>(){}.getType();
            Map<String, PlayerPoints> loaded = gson.fromJson(reader, type);
            
            if (loaded != null) {
                playerPoints.clear();
                loaded.forEach((uuidStr, points) -> {
                    try {
                        UUID uuid = UUID.fromString(uuidStr);
                        playerPoints.put(uuid, points);
                    } catch (IllegalArgumentException e) {
                        // 無効なUUIDは無視
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * JSONファイルにデータを保存する
     */
    public void saveData() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            Map<String, PlayerPoints> toSave = new HashMap<>();
            playerPoints.forEach((uuid, points) -> toSave.put(uuid.toString(), points));
            gson.toJson(toSave, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * プレイヤーのポイントを取得
     */
    public PlayerPoints getPoints(UUID uuid) {
        return playerPoints.computeIfAbsent(uuid, k -> new PlayerPoints());
    }
    
    /**
     * 通常チャットポイントを追加
     */
    public void addNormalChatPoints(UUID uuid, int points) {
        getPoints(uuid).addNormalChat(points);
    }
    
    /**
     * 挨拶ポイントを追加
     */
    public void addGreetingPoints(UUID uuid, int points) {
        getPoints(uuid).addGreeting(points);
    }
    
    /**
     * 新規さん歓迎ポイントを追加
     */
    public void addWelcomeNewPlayerPoints(UUID uuid, int points) {
        getPoints(uuid).addWelcomeNewPlayer(points);
    }
    
    /**
     * プレイヤーのポイントを取得してリセット
     */
    public PlayerPoints getAndResetPoints(UUID uuid) {
        PlayerPoints points = getPoints(uuid);
        PlayerPoints copy = new PlayerPoints(points.normalChat, points.greeting, points.welcomeNewPlayer);
        points.reset();
        return copy;
    }
}

