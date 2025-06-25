import java.util.HashSet;

public class CollisionHelper {
    public static boolean collision(Block a, Block b) {
        // Check normal collision
        if (a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y) {
            return true;
        }

        // Check wrapped horizontal collision
        int aWrappedX = a.x;
        int bWrappedX = b.x;

        if (a.x < a.width) {
            aWrappedX = a.x + GameConfig.BOARD_WIDTH;
        } else if (a.x > GameConfig.BOARD_WIDTH - a.width) {
            aWrappedX = a.x - GameConfig.BOARD_WIDTH;
        }

        if (b.x < b.width) {
            bWrappedX = b.x + GameConfig.BOARD_WIDTH;
        } else if (b.x > GameConfig.BOARD_WIDTH - b.width) {
            bWrappedX = b.x - GameConfig.BOARD_WIDTH;
        }

        return aWrappedX < bWrappedX + b.width &&
                aWrappedX + a.width > bWrappedX &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    public static Block checkCollisionWithSet(Block entity, HashSet<Block> set) {
        for (Block block : set) {
            if (collision(entity, block)) {
                return block;
            }
        }
        return null;
    }
}