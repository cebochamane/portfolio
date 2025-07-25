import java.awt.Image;

public class Block {
    public int x;
    public int y;
    public int width;
    public int height;
    public Image image;

    public int startX;
    public int startY;
    public char direction = 'U'; // U D L R
    public int velocityX = 0;
    public int velocityY = 0;

    public Block(Image image, int x, int y, int width, int height) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.startX = x;
        this.startY = y;
    }

    public void reset() {
        this.x = this.startX;
        this.y = this.startY;
    }
}