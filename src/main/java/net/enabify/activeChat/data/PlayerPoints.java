package net.enabify.activeChat.data;

/**
 * プレイヤーのポイント内訳を保持するクラス
 */
public class PlayerPoints {
    // 通常チャットのポイント
    public int normalChat = 0;
    
    // 挨拶チャットのポイント
    public int greeting = 0;
    
    // 新規さん歓迎チャットのポイント
    public int welcomeNewPlayer = 0;
    
    public PlayerPoints() {}
    
    public PlayerPoints(int normalChat, int greeting, int welcomeNewPlayer) {
        this.normalChat = normalChat;
        this.greeting = greeting;
        this.welcomeNewPlayer = welcomeNewPlayer;
    }
    
    /**
     * 総ポイント数を取得
     */
    public int getTotal() {
        return normalChat + greeting + welcomeNewPlayer;
    }
    
    /**
     * 通常チャットポイントを追加
     */
    public void addNormalChat(int points) {
        this.normalChat += points;
    }
    
    /**
     * 挨拶ポイントを追加
     */
    public void addGreeting(int points) {
        this.greeting += points;
    }
    
    /**
     * 新規さん歓迎ポイントを追加
     */
    public void addWelcomeNewPlayer(int points) {
        this.welcomeNewPlayer += points;
    }
    
    /**
     * すべてのポイントをリセット
     */
    public void reset() {
        this.normalChat = 0;
        this.greeting = 0;
        this.welcomeNewPlayer = 0;
    }
    
    /**
     * ポイント内訳を文字列で取得
     */
    public String getDetails() {
        return "- 通常チャット: " + normalChat + "円\n" +
               "- 挨拶: " + greeting + "円\n" +
               "- 新規さん挨拶: " + welcomeNewPlayer + "円";
    }
}
