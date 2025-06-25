import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private GameAssets assets;
    private GameState state;
    private javax.swing.Timer gameLoop;
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

        gameLoop = new javax.swing.Timer(50, this);
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
            ghost.direction = directions[random.nextInt(directions.length)];
            updateVelocity(ghost);

            while (CollisionHelper.checkCollisionWithSet(ghost, state.walls) != null) {
                ghost.x += GameConfig.TILE_SIZE / 4;
                ghost.y += GameConfig.TILE_SIZE / 4;
            }
        }
    }

    private Block createTestBlock(Block entity, char direction) {
        int testX = entity.x;
        int testY = entity.y;

        switch (direction) {
            case 'U':
                testY -= GameConfig.TILE_SIZE / 4;
                break;
            case 'D':
                testY += GameConfig.TILE_SIZE / 4;
                break;
            case 'L':
                testX -= GameConfig.TILE_SIZE / 4;
                break;
            case 'R':
                testX += GameConfig.TILE_SIZE / 4;
                break;
        }

        return new Block(null, testX, testY, entity.width, entity.height);
    }

    private void updateGhostDirection(Block ghost, char direction) {
        if (ghost.image == assets.redGhostImage) {
            int dx = state.pacman.x - ghost.x;
            int dy = state.pacman.y - ghost.y;

            if (random.nextInt(100) < 20) {
                direction = directions[random.nextInt(directions.length)];
            } else {
                if (Math.abs(dx) > Math.abs(dy)) {
                    direction = dx > 0 ? 'R' : 'L';
                } else {
                    direction = dy > 0 ? 'D' : 'U';
                }
            }
        }

        Block temp = createTestBlock(ghost, direction);
        if (CollisionHelper.checkCollisionWithSet(temp, state.walls) == null) {
            ghost.direction = direction;
            updateVelocity(ghost);
        } else {
            List<Character> possibleDirs = new ArrayList<>();
            for (char dir : directions) {
                temp = createTestBlock(ghost, dir);
                if (CollisionHelper.checkCollisionWithSet(temp, state.walls) == null) {
                    possibleDirs.add(dir);
                }
            }
            if (!possibleDirs.isEmpty()) {
                ghost.direction = possibleDirs.get(random.nextInt(possibleDirs.size()));
                updateVelocity(ghost);
            }
        }
    }

    private void updateVelocity(Block entity) {
        int speed = GameConfig.TILE_SIZE / 4;
        if (state.ghosts.contains(entity)) {
            speed = GameConfig.TILE_SIZE / 3;
        }

        switch (entity.direction) {
            case 'U':
                entity.velocityX = 0;
                entity.velocityY = -speed;
                break;
            case 'D':
                entity.velocityX = 0;
                entity.velocityY = speed;
                break;
            case 'L':
                entity.velocityX = -speed;
                entity.velocityY = 0;
                break;
            case 'R':
                entity.velocityX = speed;
                entity.velocityY = 0;
                break;
        }
    }

    private boolean isInTunnelArea(Block entity) {
        return (entity.y > 8 * GameConfig.TILE_SIZE && entity.y < 10 * GameConfig.TILE_SIZE) &&
                (entity.x < GameConfig.TILE_SIZE
                        || entity.x > GameConfig.BOARD_WIDTH - entity.width - GameConfig.TILE_SIZE);
    }

    private boolean isValidCherryPosition(int x, int y) {
        Block test = new Block(null, x, y, CHERRY_SIZE, CHERRY_SIZE);
        return CollisionHelper.checkCollisionWithSet(test, state.walls) == null &&
                !CollisionHelper.collision(test, state.pacman) &&
                CollisionHelper.checkCollisionWithSet(test, state.ghosts) == null &&
                !isInTunnelArea(test);
    }

    private void maybeSpawnCherry() {
        if (state.foods.size() < GameConfig.INITIAL_FOOD_COUNT / 2) {
            long currentTime = System.currentTimeMillis();
            if (state.cherry == null && currentTime - state.lastCherryTime > GameState.CHERRY_SPAWN_INTERVAL) {
                for (int i = 0; i < 20; i++) {
                    int x = (random.nextInt(GameConfig.COLUMN_COUNT - 2) + 1) * GameConfig.TILE_SIZE +
                            (GameConfig.TILE_SIZE - CHERRY_SIZE) / 2;
                    int y = (random.nextInt(GameConfig.ROW_COUNT - 2) + 1) * GameConfig.TILE_SIZE +
                            (GameConfig.TILE_SIZE - CHERRY_SIZE) / 2;

                    if (isValidCherryPosition(x, y)) {
                        state.cherry = new Block(assets.cherryImage, x, y, CHERRY_SIZE, CHERRY_SIZE);
                        state.lastCherryTime = currentTime;
                        break;
                    }
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Draw walls first
        for (Block wall : state.walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        // Draw food
        g.setColor(Color.WHITE);
        for (Block food : state.foods) {
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        // Draw cherry if exists
        if (state.cherry != null) {
            g.drawImage(state.cherry.image, state.cherry.x, state.cherry.y,
                    state.cherry.width, state.cherry.height, null);
        }

        // Draw ghosts
        for (Block ghost : state.ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        // Draw Pacman
        g.drawImage(state.pacman.image, state.pacman.x, state.pacman.y,
                state.pacman.width, state.pacman.height, null);

        // Draw UI
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.setColor(Color.WHITE);
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

        // Wrap around
        if (state.pacman.x + state.pacman.width < 0) {
            state.pacman.x = GameConfig.BOARD_WIDTH;
        } else if (state.pacman.x > GameConfig.BOARD_WIDTH) {
            state.pacman.x = -state.pacman.width;
        }

        // Wall collision for Pacman
        if (CollisionHelper.checkCollisionWithSet(state.pacman, state.walls) != null) {
            state.pacman.x -= state.pacman.velocityX;
            state.pacman.y -= state.pacman.velocityY;
        }

        // Ghost movement
        for (Block ghost : state.ghosts) {
            if (isInTunnelArea(ghost) && random.nextInt(10) < 3) {
                ghost.direction = ghost.y < 9 * GameConfig.TILE_SIZE ? 'D' : 'U';
                updateVelocity(ghost);
            }

            if (random.nextInt(100) < 5) {
                updateGhostDirection(ghost, ghost.direction);
            }

            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            // Wrap around
            if (ghost.x + ghost.width < 0) {
                ghost.x = GameConfig.BOARD_WIDTH;
            } else if (ghost.x > GameConfig.BOARD_WIDTH) {
                ghost.x = -ghost.width;
            }

            // Wall collision handling
            if (CollisionHelper.checkCollisionWithSet(ghost, state.walls) != null) {
                ghost.x -= ghost.velocityX;
                ghost.y -= ghost.velocityY;
                updateGhostDirection(ghost, directions[random.nextInt(directions.length)]);
            }

            // Ghost-Pacman collision
            if (CollisionHelper.collision(ghost, state.pacman)) {
                state.lives--;
                if (state.lives == 0) {
                    state.gameOver = true;
                    state.updateHighScore();
                }
                resetPositions();
            }
        }

        // Food collision
        Block foodEaten = CollisionHelper.checkCollisionWithSet(state.pacman, state.foods);
        if (foodEaten != null) {
            state.foods.remove(foodEaten);
            state.score += 10;
        }

        // Cherry collision
        if (state.cherry != null && CollisionHelper.collision(state.pacman, state.cherry)) {
            state.score += CHERRY_SCORE;
            state.cherry = null;
        }

        // Spawn cherry
        maybeSpawnCherry();

        // Level complete
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
            updateGhostDirection(ghost, directions[random.nextInt(directions.length)]);
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
                break;
            case KeyEvent.VK_DOWN:
                updatePacmanDirection('D');
                break;
            case KeyEvent.VK_LEFT:
                updatePacmanDirection('L');
                break;
            case KeyEvent.VK_RIGHT:
                updatePacmanDirection('R');
                break;
        }
    }

    private void updatePacmanDirection(char direction) {
        Block temp = createTestBlock(state.pacman, direction);
        if (CollisionHelper.checkCollisionWithSet(temp, state.walls) == null) {
            state.pacman.direction = direction;
            updateVelocity(state.pacman);

            switch (direction) {
                case 'U':
                    state.pacman.image = assets.pacmanUpImage;
                    break;
                case 'D':
                    state.pacman.image = assets.pacmanDownImage;
                    break;
                case 'L':
                    state.pacman.image = assets.pacmanLeftImage;
                    break;
                case 'R':
                    state.pacman.image = assets.pacmanRightImage;
                    break;
            }
        }
    }
}