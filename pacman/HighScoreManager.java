import java.io.*;

public class HighScoreManager {
    private static final String HIGH_SCORE_FILE = "highscore.dat";

    public static void saveHighScore(int highScore) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(HIGH_SCORE_FILE))) {
            oos.writeInt(highScore);
        } catch (IOException e) {
            System.out.println("Unable to save high score: " + e.getMessage());
        }
    }

    public static int loadHighScore() {
        File file = new File(HIGH_SCORE_FILE);
        if (!file.exists()) {
            return 0;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(HIGH_SCORE_FILE))) {
            return ois.readInt();
        } catch (IOException e) {
            System.out.println("Unable to load high score: " + e.getMessage());
            return 0;
        }
    }
}