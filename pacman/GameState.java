import java.util.HashSet;

public class GameState {
    public HashSet<Block> walls;
    public HashSet<Block> foods;
    public HashSet<Block> ghosts;
    public Block pacman;
    public Block cherry;

    public int score = 0;
    public int highScore = HighScoreManager.loadHighScore();
    public int lives = 3;
    public boolean gameOver = false;
    public long lastCherryTime = 0;
    public static final long CHERRY_SPAWN_INTERVAL = 10000; // 10 seconds

    public void resetGame() {
        score = 0;
        lives = 3;
        gameOver = false;
        cherry = null;
        lastCherryTime = 0;
    }

    public void updateHighScore() {
        if (score > highScore) {
            highScore = score;
            HighScoreManager.saveHighScore(highScore);
        }
    }
}