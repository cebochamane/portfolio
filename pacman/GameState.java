import java.awt.*;
import java.util.HashSet;
import java.util.Random;

public class GameState {
    private final HashSet<Block> walls;
    private final HashSet<Block> foods;
    private final HashSet<Block> ghosts;
    private final HashSet<Block> cherries;
    private Block pacman;
    private int score = 0;
    private int highScore = 0;
    private int lives = 3;
    private boolean gameOver = false;
    private boolean isCherryActive = false;
    private long cherryEndTime = 0;
    private final Random random = new Random();

    public GameState() {
        walls = new HashSet<>();
        foods = new HashSet<>();
        ghosts = new HashSet<>();
        cherries = new HashSet<>();
        loadMap();
    }

    public void loadMap() {
        walls.clear();
        foods.clear();
        ghosts.clear();
        cherries.clear();

        for (int r = 0; r < GameConfig.ROW_COUNT; r++) {
            for (int c = 0; c < GameConfig.COLUMN_COUNT; c++) {
                char tileMapChar = GameConfig.TILE_MAP[r].charAt(c);
                int x = c * GameConfig.TILE_SIZE;
                int y = r * GameConfig.TILE_SIZE;

                switch (tileMapChar) {
                    case 'X' ->
                        walls.add(new Block(GameAssets.WALL_IMAGE, x, y, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE));
                    case 'b' -> ghosts.add(
                            new Block(GameAssets.BLUE_GHOST_IMAGE, x, y, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE));
                    case 'o' -> ghosts.add(
                            new Block(GameAssets.ORANGE_GHOST_IMAGE, x, y, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE));
                    case 'p' -> ghosts.add(
                            new Block(GameAssets.PINK_GHOST_IMAGE, x, y, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE));
                    case 'r' -> ghosts.add(
                            new Block(GameAssets.RED_GHOST_IMAGE, x, y, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE));
                    case 'P' -> pacman = new Block(GameAssets.PACMAN_RIGHT_IMAGE, x, y, GameConfig.TILE_SIZE,
                            GameConfig.TILE_SIZE);
                    case ' ' -> foods.add(new Block(null, x + 14, y + 14, 4, 4));
                    case 'C' -> cherries
                            .add(new Block(GameAssets.CHERRY_IMAGE, x, y, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE));
                }
            }
        }
    }

    public void update() {
        movePacman();
        moveGhosts();
        checkCollisions();
        checkCherryStatus();
    }

    private void movePacman() {
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        for (Block wall : walls) {
            if (CollisionHelper.checkCollision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }
    }

    private void moveGhosts() {
        for (Block ghost : ghosts) {
            if (ghost.y == GameConfig.TILE_SIZE * 9 && ghost.direction != 'U' && ghost.direction != 'D') {
                ghost.updateDirection('U');
            }

            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            for (Block wall : walls) {
                if (CollisionHelper.checkCollision(ghost, wall) || ghost.x <= 0
                        || ghost.x + ghost.width >= GameConfig.BOARD_WIDTH) {
                    ghost.x -= ghost.velocityX;
                    ghost.y -= ghost.velocityY;
                    char newDirection = GameConfig.DIRECTIONS[random.nextInt(4)];
                    ghost.updateDirection(newDirection);
                }
            }
        }
    }

    private void checkCollisions() {
        checkFoodCollision();
        checkCherryCollision();
        checkGhostCollision();
    }

    private void checkFoodCollision() {
        Block foodEaten = null;
        for (Block food : foods) {
            if (CollisionHelper.checkCollision(pacman, food)) {
                foodEaten = food;
                score += 10;
            }
        }
        foods.remove(foodEaten);

        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    private void checkCherryCollision() {
        Block cherryEaten = null;
        for (Block cherry : cherries) {
            if (CollisionHelper.checkCollision(pacman, cherry)) {
                cherryEaten = cherry;
                activateCherryPower();
            }
        }
        cherries.remove(cherryEaten);
    }

    private void checkGhostCollision() {
        for (Block ghost : ghosts) {
            if (CollisionHelper.checkCollision(ghost, pacman)) {
                if (isCherryActive) {
                    ghost.reset();
                    score += 50;
                } else {
                    lives--;
                    if (lives == 0) {
                        gameOver = true;
                        if (score > highScore) {
                            highScore = score;
                        }
                        return;
                    }
                    resetPositions();
                }
            }
        }
    }

    private void checkCherryStatus() {
        if (isCherryActive && System.currentTimeMillis() > cherryEndTime) {
            isCherryActive = false;
        }
    }

    private void activateCherryPower() {
        isCherryActive = true;
        cherryEndTime = System.currentTimeMillis() + 5000; // 5 seconds
        score += 20;
    }

    public void draw(Graphics g) {
        drawPacman(g);
        drawGhosts(g);
        drawWalls(g);
        drawFood(g);
        drawCherries(g);
        drawUI(g);
    }

    private void drawPacman(Graphics g) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);
    }

    private void drawGhosts(Graphics g) {
        for (Block ghost : ghosts) {
            if (isCherryActive) {
                g.drawImage(GameAssets.VULNERABLE_GHOST_IMAGE, ghost.x, ghost.y, ghost.width, ghost.height, null);
            } else {
                g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
            }
        }
    }

    private void drawWalls(Graphics g) {
        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }
    }

    private void drawFood(Graphics g) {
        g.setColor(Color.WHITE);
        for (Block food : foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }
    }

    private void drawCherries(Graphics g) {
        for (Block cherry : cherries) {
            g.drawImage(cherry.image, cherry.x, cherry.y, cherry.width, cherry.height, null);
        }
    }

    private void drawUI(Graphics g) {
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            g.drawString("Game Over! Score: " + score + " High Score: " + highScore,
                    GameConfig.TILE_SIZE / 2, GameConfig.TILE_SIZE / 2);
        } else {
            String status = isCherryActive ? "CHERRY ACTIVE! " : "";
            g.drawString(status + "Lives: " + lives + " Score: " + score + " High: " + highScore,
                    GameConfig.TILE_SIZE / 2, GameConfig.TILE_SIZE / 2);
        }
    }

    public void handlePlayerInput(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP -> pacman.updateDirection('U');
            case KeyEvent.VK_DOWN -> pacman.updateDirection('D');
            case KeyEvent.VK_LEFT -> pacman.updateDirection('L');
            case KeyEvent.VK_RIGHT -> pacman.updateDirection('R');
        }

        switch (pacman.direction) {
            case 'U' -> pacman.image = GameAssets.PACMAN_UP_IMAGE;
            case 'D' -> pacman.image = GameAssets.PACMAN_DOWN_IMAGE;
            case 'L' -> pacman.image = GameAssets.PACMAN_LEFT_IMAGE;
            case 'R' -> pacman.image = GameAssets.PACMAN_RIGHT_IMAGE;
        }
    }

    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;

        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = GameConfig.DIRECTIONS[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    public void resetGame() {
        score = 0;
        lives = 3;
        gameOver = false;
        isCherryActive = false;
        loadMap();
        resetPositions();
    }

    public boolean isGameOver() {
        return gameOver;
    }
}