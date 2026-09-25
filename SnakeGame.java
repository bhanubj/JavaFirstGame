import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Random;

/**
 * Simple Snake Game
 * Run with:  java SnakeGame
 * (compile with: javac SnakeGame.java)
 */
public class SnakeGame extends JPanel implements ActionListener {

    private final int TILE_SIZE = 25;
    private final int GRID_WIDTH = 24;   // number of tiles horizontally
    private final int GRID_HEIGHT = 20;  // number of tiles vertically
    private final int BOARD_WIDTH = TILE_SIZE * GRID_WIDTH;
    private final int BOARD_HEIGHT = TILE_SIZE * GRID_HEIGHT;

    private final LinkedList<Point> snake = new LinkedList<>();
    private Point food;
    private final Random random = new Random();

    // direction: 0 = up, 1 = right, 2 = down, 3 = left
    private int direction = 1;
    private boolean directionChangedThisTick = false;

    private boolean gameOver = false;
    private boolean paused = false;
    private int score = 0;

    private Timer timer;
    private final int INITIAL_DELAY = 150; // ms per tick
    private int currentDelay = INITIAL_DELAY;

    public SnakeGame() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setBackground(new Color(20, 20, 20));
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKey(e.getKeyCode());
            }
        });

        initGame();
    }

    private void initGame() {
        snake.clear();
        int startX = GRID_WIDTH / 2;
        int startY = GRID_HEIGHT / 2;
        snake.add(new Point(startX, startY));
        snake.add(new Point(startX - 1, startY));
        snake.add(new Point(startX - 2, startY));

        direction = 1;
        score = 0;
        gameOver = false;
        paused = false;
        currentDelay = INITIAL_DELAY;

        spawnFood();

        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(currentDelay, this);
        timer.start();
    }

    private void spawnFood() {
        Point newFood;
        do {
            int x = random.nextInt(GRID_WIDTH);
            int y = random.nextInt(GRID_HEIGHT);
            newFood = new Point(x, y);
        } while (snake.contains(newFood));
        food = newFood;
    }

    private void handleKey(int keyCode) {
        if (keyCode == KeyEvent.VK_R) {
            initGame();
            repaint();
            return;
        }

        if (keyCode == KeyEvent.VK_P) {
            if (!gameOver) {
                paused = !paused;
                repaint();
            }
            return;
        }

        if (gameOver || paused || directionChangedThisTick) {
            return;
        }

        int newDirection = direction;
        switch (keyCode) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                newDirection = 0;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                newDirection = 1;
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                newDirection = 2;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                newDirection = 3;
                break;
            default:
                return;
        }

        // Prevent reversing directly into itself
        if (Math.abs(newDirection - direction) != 2) {
            direction = newDirection;
            directionChangedThisTick = true;
        }
    }

    private void moveSnake() {
        Point head = snake.getFirst();
        Point newHead;

        switch (direction) {
            case 0: newHead = new Point(head.x, head.y - 1); break;
            case 1: newHead = new Point(head.x + 1, head.y); break;
            case 2: newHead = new Point(head.x, head.y + 1); break;
            case 3: newHead = new Point(head.x - 1, head.y); break;
            default: newHead = head;
        }

        // Wall collision
        if (newHead.x < 0 || newHead.x >= GRID_WIDTH || newHead.y < 0 || newHead.y >= GRID_HEIGHT) {
            gameOver = true;
            timer.stop();
            return;
        }

        // Self collision
        if (snake.contains(newHead)) {
            gameOver = true;
            timer.stop();
            return;
        }

        snake.addFirst(newHead);

        if (newHead.equals(food)) {
            score += 10;
            spawnFood();
            speedUp();
        } else {
            snake.removeLast();
        }
    }

    private void speedUp() {
        if (currentDelay > 60) {
            currentDelay -= 4;
            timer.setDelay(currentDelay);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver && !paused) {
            moveSnake();
        }
        directionChangedThisTick = false;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2);
        drawFood(g2);
        drawSnake(g2);
        drawScore(g2);

        if (paused && !gameOver) {
            drawCenteredOverlay(g2, "PAUSED", "Press P to resume");
        }

        if (gameOver) {
            drawCenteredOverlay(g2, "GAME OVER", "Score: " + score + "   |   Press R to restart");
        }
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(new Color(35, 35, 35));
        for (int x = 0; x <= GRID_WIDTH; x++) {
            g2.drawLine(x * TILE_SIZE, 0, x * TILE_SIZE, BOARD_HEIGHT);
        }
        for (int y = 0; y <= GRID_HEIGHT; y++) {
            g2.drawLine(0, y * TILE_SIZE, BOARD_WIDTH, y * TILE_SIZE);
        }
    }

    private void drawFood(Graphics2D g2) {
        g2.setColor(new Color(220, 60, 60));
        g2.fillOval(food.x * TILE_SIZE + 3, food.y * TILE_SIZE + 3, TILE_SIZE - 6, TILE_SIZE - 6);
    }

    private void drawSnake(Graphics2D g2) {
        boolean isHead = true;
        for (Point p : snake) {
            if (isHead) {
                g2.setColor(new Color(90, 210, 120));
                isHead = false;
            } else {
                g2.setColor(new Color(60, 170, 90));
            }
            g2.fillRoundRect(p.x * TILE_SIZE + 1, p.y * TILE_SIZE + 1, TILE_SIZE - 2, TILE_SIZE - 2, 6, 6);
        }
    }

    private void drawScore(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.BOLD, 16));
        g2.drawString("Score: " + score, 10, 20);
    }

    private void drawCenteredOverlay(Graphics2D g2, String title, String subtitle) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Consolas", Font.BOLD, 32));
        FontMetrics fm = g2.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g2.drawString(title, (BOARD_WIDTH - titleWidth) / 2, BOARD_HEIGHT / 2 - 10);

        g2.setFont(new Font("Consolas", Font.PLAIN, 16));
        fm = g2.getFontMetrics();
        int subtitleWidth = fm.stringWidth(subtitle);
        g2.drawString(subtitle, (BOARD_WIDTH - subtitleWidth) / 2, BOARD_HEIGHT / 2 + 20);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Snake Game");
            SnakeGame game = new SnakeGame();

            frame.add(game);
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

            game.requestFocusInWindow();
        });
    }
}
