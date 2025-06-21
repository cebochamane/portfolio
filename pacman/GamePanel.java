import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private final GameState gameState;
    private final Timer gameLoop;
    private boolean isPaused = false;

    public GamePanel() {
        setPreferredSize(new Dimension(GameConfig.BOARD_WIDTH, GameConfig.BOARD_HEIGHT));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        gameState = new GameState();
        gameLoop = new Timer(50, this); // 20fps
        gameLoop.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        gameState.draw(g);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isPaused && !gameState.isGameOver()) {
            gameState.update();
        }
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_P) {
            isPaused = !isPaused;
        }

        if (!isPaused && !gameState.isGameOver()) {
            gameState.handlePlayerInput(e.getKeyCode());
        } else if (gameState.isGameOver()) {
            gameState.resetGame();
            isPaused = false;
            gameLoop.start();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}