import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private GameAssets assets;
    private GameState state;
    private Timer gameLoop;
    private Random random;
    private char[] directions = { 'U', 'D', 'L', 'R' };
    private static final int CHERRY_SCORE = 500;
    private static final int CHERRY_SIZE = 20;

    public GamePanel() {
        setPreferredSize(new Dimension(GameConfig.BOARD_WIDTH, GameConfig.BOARD_HEIGHT));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        assets = new GameAssets();
        state = new GameState();
        random = new Random();

        loadMap();
        initializeGhosts();
        state.highScore = HighScoreManager.loadHighScore();

        gameLoop = new Timer(50, this); // 20fps
        gameLoop.start();
    }

    private void loadMap() {
        state.walls = new HashSet<>();
        state.foods = new HashSet<>();
        state.ghosts = new HashSet<>();

        for (int r = 0; r < GameConfig.ROW_COUNT; r++) {
            for (int c = 0; c < GameConfig.COLUMN_COUNT; c++) {
                char tileChar = GameConfig.TILE_MAP[r].charAt(c);
                int x = c * GameConfig.TILE_SIZE;
                int y = r * GameConfig.TILE_SIZE;

                switch (tileChar) {
                    case 'X':
                        state.walls.add(new Block(assets.wallImage, x, y, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE));
                        break;
                    case 'b':
                        state.ghosts.add(
                                new Block(assets.blueGhostImage, x, y, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE));
                        break;
                    case 'o':
                        state.ghosts.add(
                                new Block(assets.orangeGhostImage, x, y, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE));
                        break;
                    case 'p':
                        state.ghosts.add(
                                new Block(assets.pinkGhostImage, x, y, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE));
                        break;
                    case 'r':
                        state.ghosts
                                .add(new Block(assets.redGhostImage, x, y, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE));
                        break;
                    case 'P':
                        state.pacman = new Block(assets.pacmanRightImage, x, y, GameConfig.TILE_SIZE,
                                GameConfig.TILE_SIZE);
                        break;
                    case ' ':
                        state.foods.add(new Block(null, x + 14, y + 14, 4, 4));
                        break;
                }
            }
        }
    }

    private void initializeGhosts() {
        for (Block ghost : state.ghosts) {
            char newDirection = directions[random.nextInt(4)];
            updateGhostDirection(ghost, newDirection);
        }
    }

    private void updateGhostDirection(Block ghost, char direction) {
        char prevDirection = ghost.direction;
        ghost.direction = direction;
        updateVelocity(ghost);

        ghost.x += ghost.velocityX;
        ghost.y += ghost.velocityY;

        Block collidedWall = CollisionHelper.checkCollisionWithSet(ghost, state.walls);
        if (collidedWall != null) {
            ghost.x -= ghost.velocityX;
            ghost.y -= ghost.velocityY;
            ghost.direction = prevDirection;
            updateVelocity(ghost);
        }
    }

    private void updateVelocity(Block entity) {
        switch (entity.direction) {
            case 'U':
                entity.velocityX = 0;
                entity.velocityY = -GameConfig.TILE_SIZE / 4;
                break;
            case 'D':
                entity.velocityX = 0;
                entity.velocityY = GameConfig.TILE_SIZE / 4;
                break;
            case 'L':
                entity.velocityX = -GameConfig.TILE_SIZE / 4;
                entity.velocityY = 0;
                break;
            case 'R':
                entity.velocityX = GameConfig.TILE_SIZE / 4;
                entity.velocityY = 0;
                break;
        }
    }

    private void spawnCherry() {
        long currentTime = System.currentTimeMillis();
        if (state.cherry == null &&
                currentTime - state.lastCherryTime > GameState.CHERRY_SPAWN_INTERVAL) {

            // Find a random position not occupied by walls
            int attempts = 0;
            while (attempts < 100) { // Try 100 times to find a valid position
                int x = random.nextInt(GameConfig.BOARD_WIDTH - CHERRY_SIZE);
                int y = random.nextInt(GameConfig.BOARD_HEIGHT - CHERRY_SIZE);

                Block tempCherry = new Block(assets.cherryImage, x, y, CHERRY_SIZE, CHERRY_SIZE);
                if (CollisionHelper.checkCollisionWithSet(tempCherry, state.walls) == null) {
                    state.cherry = tempCherry;
                    state.lastCherryTime = currentTime;
                    break;
                }
                attempts++;
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Draw Pacman
        g.drawImage(state.pacman.image, state.pacman.x, state.pacman.y,
                state.pacman.width, state.pacman.height, null);

        // Draw ghosts
        for (Block ghost : state.ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        // Draw walls
        for (Block wall : state.walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }
        // Draw cherry
        if (state.cherry != null) {
            g.drawImage(state.cherry.image, state.cherry.x, state.cherry.y,
                    state.cherry.width, state.cherry.height, null);
        }
        // Draw food
        g.setColor(Color.WHITE);
        for (Block food : state.foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (state.gameOver) {
            g.drawString("Game Over! High Score: " + state.highScore,
                    GameConfig.TILE_SIZE / 2, GameConfig.TILE_SIZE / 2);
            g.drawString("Your Score: " + state.score,
                    GameConfig.TILE_SIZE / 2, GameConfig.TILE_SIZE / 2 + 20);
        } else {
            g.drawString("Lives: x" + state.lives, GameConfig.TILE_SIZE / 2, GameConfig.TILE_SIZE / 2);
            g.drawString("Score: " + state.score, GameConfig.TILE_SIZE / 2 + 100, GameConfig.TILE_SIZE / 2);
            g.drawString("High Score: " + state.highScore,
                    GameConfig.BOARD_WIDTH - 150, GameConfig.TILE_SIZE / 2);
        }
    }

    public void move() {
        // Move Pacman
        state.pacman.x += state.pacman.velocityX;
        state.pacman.y += state.pacman.velocityY;

        // Check wall collisions for Pacman
        Block collidedWall = CollisionHelper.checkCollisionWithSet(state.pacman, state.walls);
        if (collidedWall != null) {
            state.pacman.x -= state.pacman.velocityX;
            state.pacman.y -= state.pacman.velocityY;
        }

        // Check ghost collisions
        for (Block ghost : state.ghosts) {
            if (CollisionHelper.collision(ghost, state.pacman)) {
                state.lives -= 1;
                if (state.lives == 0) {
                    state.gameOver = true;
                    state.updateHighScore();
                    HighScoreManager.saveHighScore(state.highScore);
                }
                resetPositions();
            }

            // Special case for ghost movement
            if (ghost.y == GameConfig.TILE_SIZE * 9 && ghost.direction != 'U' && ghost.direction != 'D') {
                updateGhostDirection(ghost, 'U');
            }

            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            collidedWall = CollisionHelper.checkCollisionWithSet(ghost, state.walls);
            if (collidedWall != null || ghost.x <= 0 || ghost.x + ghost.width >= GameConfig.BOARD_WIDTH) {
                ghost.x -= ghost.velocityX;
                ghost.y -= ghost.velocityY;
                char newDirection = directions[random.nextInt(4)];
                updateGhostDirection(ghost, newDirection);
            }
        }

        // Check food collision
        Block foodEaten = CollisionHelper.checkCollisionWithSet(state.pacman, state.foods);
        if (foodEaten != null) {
            state.foods.remove(foodEaten);
            state.score += 10;
        }

        // Check cherry collision
        if (state.cherry != null && CollisionHelper.collision(state.pacman, state.cherry)) {
            state.score += CHERRY_SCORE;
            state.cherry = null;
        }

        // Maybe spawn a new cherry
        spawnCherry();
        // Check if level is complete
        if (state.foods.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    public void resetPositions() {
        state.pacman.reset();
        state.pacman.velocityX = 0;
        state.pacman.velocityY = 0;

        for (Block ghost : state.ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            updateGhostDirection(ghost, newDirection);
        }
        state.cherry = null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!state.gameOver) {
            move();
        }
        repaint();

        if (state.gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (state.gameOver) {
            loadMap();
            resetPositions();
            state.resetGame();
            gameLoop.start();
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                updatePacmanDirection('U');
                state.pacman.image = assets.pacmanUpImage;
                break;
            case KeyEvent.VK_DOWN:
                updatePacmanDirection('D');
                state.pacman.image = assets.pacmanDownImage;
                break;
            case KeyEvent.VK_LEFT:
                updatePacmanDirection('L');
                state.pacman.image = assets.pacmanLeftImage;
                break;
            case KeyEvent.VK_RIGHT:
                updatePacmanDirection('R');
                state.pacman.image = assets.pacmanRightImage;
                break;
        }
    }

    private void updatePacmanDirection(char direction) {
        char prevDirection = state.pacman.direction;
        state.pacman.direction = direction;
        updateVelocity(state.pacman);

        state.pacman.x += state.pacman.velocityX;
        state.pacman.y += state.pacman.velocityY;

        Block collidedWall = CollisionHelper.checkCollisionWithSet(state.pacman, state.walls);
        if (collidedWall != null) {
            state.pacman.x -= state.pacman.velocityX;
            state.pacman.y -= state.pacman.velocityY;
            state.pacman.direction = prevDirection;
            updateVelocity(state.pacman);
        }
    }
}