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
    private final Map<UUID, Integer> playerPoints;
    
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
            Type type = new TypeToken<Map<String, Integer>>(){}.getType();
            Map<String, Integer> loaded = gson.fromJson(reader, type);
            
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
            Map<String, Integer> toSave = new HashMap<>();
            playerPoints.forEach((uuid, points) -> toSave.put(uuid.toString(), points));
            gson.toJson(toSave, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * プレイヤーのポイントを取得
     */
    public int getPoints(UUID uuid) {
        return playerPoints.getOrDefault(uuid, 0);
    }
    
    /**
     * プレイヤーのポイントを追加
     */
    public void addPoints(UUID uuid, int points) {
        playerPoints.merge(uuid, points, Integer::sum);
    }
    
    /**
     * プレイヤーのポイントをリセット
     */
    public void resetPoints(UUID uuid) {
        playerPoints.put(uuid, 0);
    }
    
    /**
     * プレイヤーのポイントを取得してリセット
     */
    public int getAndResetPoints(UUID uuid) {
        int points = getPoints(uuid);
        resetPoints(uuid);
        return points;
    }
}
