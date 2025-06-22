import java.util.HashSet;

public class CollisionHelper {
    public static boolean collision(Block a, Block b) {
        return a.x < b.x + b.width &&
                a.x + a.width > b.x &&
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