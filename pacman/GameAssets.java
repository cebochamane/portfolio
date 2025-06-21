import javax.swing.ImageIcon;
import java.awt.Image;

public class GameAssets {
    public static final Image WALL_IMAGE = loadImage("wall.png");
    public static final Image BLUE_GHOST_IMAGE = loadImage("blueGhost.png");
    public static final Image ORANGE_GHOST_IMAGE = loadImage("orangeGhost.png");
    public static final Image PINK_GHOST_IMAGE = loadImage("pinkGhost.png");
    public static final Image RED_GHOST_IMAGE = loadImage("redGhost.png");
    public static final Image VULNERABLE_GHOST_IMAGE = loadImage("scaredGhost.png");
    public static final Image CHERRY_IMAGE = loadImage("cherry.png");
    public static final Image PACMAN_UP_IMAGE = loadImage("pacmanUp.png");
    public static final Image PACMAN_DOWN_IMAGE = loadImage("pacmanDown.png");
    public static final Image PACMAN_LEFT_IMAGE = loadImage("pacmanLeft.png");
    public static final Image PACMAN_RIGHT_IMAGE = loadImage("pacmanRight.png");

    private static Image loadImage(String filename) {
        return new ImageIcon(GameAssets.class.getResource("./" + filename)).getImage();
    }
}