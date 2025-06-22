import java.util.HashSet;

public class GameState {
    public HashSet<Block> walls;
    public HashSet<Block> foods;
    public HashSet<Block> ghosts;
    public Block pacman;

    public int score = 0;
    public int highScore = 0;
    public int lives = 3;
    public boolean gameOver = false;

    public void resetGame() {
        score = 0;
        lives = 3;
        gameOver = false;
    }

    public void updateHighScore() {
        if (score > highScore) {
            highScore = score;
        }
    }
}