import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.io.*;
import java.awt.image.BufferedImage; // Import dla obrazów
import javax.imageio.ImageIO; // Import dla wczytywania obrazów

public class FishingMinigame extends JPanel implements ActionListener, KeyListener {
    private int barY, fishY;
    private int fishSpeed = 2;
    private int barVelocity = 0;
    private int gravity = 1;
    private int jumpStrength = -10;
    private int barHeight = 60;
    private int fishHeight = 40;
    private int score = 0;
    private static int fishCaught = 0;
    private Timer timer;
    private Timer gameTimer;
    private int timeLeft = 10;  // Zmieniamy czas na 10 sekund
    private boolean gameRunning = false;
    private boolean firstJump = false;
    private final int FRAME_TOP = 50;
    private final int FRAME_BOTTOM = 350;
    private Random random;
    private static final String SAVE_FILE = "fish_caught.txt";
    private BoatGame boatGame;
    private JFrame fishingFrame;
    private boolean isRedFish; // New field

    // Obrazki pixel art dla minigry
    private BufferedImage minigameBarImage;
    private BufferedImage minigameFishImage;
    private BufferedImage minigameRedFishImage;

    public FishingMinigame(BoatGame boatGame, JFrame fishingFrame, boolean isRedFish) {  // Modified constructor
        this.boatGame = boatGame;
        this.fishingFrame = fishingFrame;
        this.isRedFish = isRedFish;  // Store the fish type
        barY = 120;
        fishY = new Random().nextInt(FRAME_BOTTOM - FRAME_TOP - fishHeight) + FRAME_TOP;
        timer = new Timer(30, this);
        gameTimer = new Timer(1000, e -> updateGameTimer());
        setFocusable(true);
        addKeyListener(this);
        random = new Random();
        loadFishCaught();

        // Wczytywanie obrazków pixel art dla minigry
        try {
            minigameBarImage = ImageIO.read(new File("minigame_bar_pixel.png")); // Obrazek paska gracza
            minigameFishImage = ImageIO.read(new File("minigame_fish_pixel.png")); // Obrazek zwykłej ryby w minigrze
            minigameRedFishImage = ImageIO.read(new File("minigame_red_fish_pixel.png")); // Obrazek czerwonej ryby w minigrze
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Błąd wczytywania obrazów pixel art dla minigry!");
        }
    }

    public static int getFishCaughtStatic() {
        return fishCaught;
    }

    private void updateGameTimer() {
        if (gameRunning) {
            timeLeft--;
            if (timeLeft <= 0) {
                gameRunning = false;
                timer.stop();
                gameTimer.stop();
                // Zamiast JOptionPane.showMessageDialog, użyj własnego okna dialogowego lub komunikatu na ekranie
                // JOptionPane.showMessageDialog(this, "Czas minął! Nie złapałeś ryby.");
                displayMessage("Czas minął! Nie złapałeś ryby.");
                closeFishingMinigame();
            }
        }
        repaint();
    }

    private void restartGame() {
        barY = 120;
        barVelocity = 0;
        firstJump = false;
        fishY = new Random().nextInt(FRAME_BOTTOM - FRAME_TOP - fishHeight) + FRAME_TOP;
        score = 0;
        timeLeft = 10;  // Zmieniamy czas na 10 sekund
        gameRunning = false;
        timer.stop();
        gameTimer.stop();
        requestFocusInWindow();
        repaint();
    }

    private void saveFishCaught() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SAVE_FILE))) {
            writer.write(String.valueOf(fishCaught));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFishCaught() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SAVE_FILE))) {
            String line = reader.readLine();
            if (line != null) {
                fishCaught = Integer.parseInt(line);
            }
        } catch (IOException e) {
            fishCaught = 0;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Ustawienia dla rysowania pixel art (wyłączenie antyaliasingu i interpolacji)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Rysowanie tła minigry
        g.setColor(new Color(173, 216, 230)); // Jasnoniebieskie tło
        g.fillRect(0, 0, getWidth(), getHeight());

        // Rysowanie ramki minigry (ściany)
        g.setColor(Color.DARK_GRAY);
        g.fillRect(45, FRAME_TOP, 5, FRAME_BOTTOM - FRAME_TOP); // Lewa ściana
        g.fillRect(45 + 30 - 5, FRAME_TOP, 5, FRAME_BOTTOM - FRAME_TOP); // Prawa ściana (dla obszaru 30px szerokości)

        // Rysowanie paska gracza
        if (minigameBarImage != null) {
            g2d.drawImage(minigameBarImage, 50, barY, 20, barHeight, this);
        } else {
            g.setColor(Color.GREEN);
            g.fillRect(50, barY, 20, barHeight);
        }

        // Rysowanie ryby w minigrze
        BufferedImage currentFishImage = isRedFish ? minigameRedFishImage : minigameFishImage;
        if (currentFishImage != null) {
            g2d.drawImage(currentFishImage, 50, fishY, 20, fishHeight, this);
        } else {
            g.setColor(isRedFish ? Color.RED : Color.ORANGE);
            g.fillRect(50, fishY, 20, fishHeight);
        }

        // Rysowanie paska postępu (score)
        g.setColor(Color.GRAY);
        g.fillRect(80, 20, 100, 10);
        g.setColor(Color.BLUE);
        g.fillRect(80, 20, score, 10);

        // Wyświetlanie tekstu
        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospaced", Font.BOLD, 16)); // Czcionka stylizowana na pixel art
        g.drawString("Pozostały czas: " + timeLeft + "s", 100, 50);
        g.drawString("Złapane ryby: " + fishCaught, 100, 70);

        if (!firstJump) {
            g.setColor(new Color(255, 255, 0, 150)); // Żółty z przezroczystością
            g.fillRect(30, 200, 140, 50);
            g.setColor(Color.BLACK);
            g.drawString("Naciśnij SPACE by zacząć", 40, 230);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameRunning)
            return;

        // Increase fish speed if it's a red fish
        fishY += isRedFish ? fishSpeed * 2 : fishSpeed;

        if (fishY <= FRAME_TOP || fishY >= FRAME_BOTTOM - fishHeight) {
            fishSpeed = -fishSpeed;
        }

        if (random.nextInt(100) < 5) {
            fishSpeed = random.nextBoolean() ? 2 : -2;
        }

        if (firstJump) {
            barVelocity += gravity;
            barY += barVelocity;
        }

        if (barY > FRAME_BOTTOM - barHeight) {
            barY = FRAME_BOTTOM - barHeight;
            barVelocity = 0;
        } else if (barY < FRAME_TOP) {
            barY = FRAME_TOP;
            barVelocity = 0;
        }

        if (new Rectangle(50, barY, 20, barHeight).intersects(new Rectangle(50, fishY, 20, fishHeight))) {
            score = Math.min(score + 1, 100);
            if (score >= 100) {
                gameRunning = false;
                timer.stop();
                gameTimer.stop();
                if (score > 0) {  // Tylko wtedy, gdy gracz naprawdę złapał jakąś rybę
                    if (isRedFish) {
                        fishCaught += 2;
                    } else {
                        fishCaught++;
                    }
                    saveFishCaught();  // Zapisz wynik tylko wtedy, gdy złapano rybę
                }
                // Zamiast JOptionPane.showMessageDialog, użyj własnego okna dialogowego lub komunikatu na ekranie
                // JOptionPane.showMessageDialog(this, "Złapałeś rybę!");
                displayMessage("Złapałeś rybę!");
                closeFishingMinigame();
            }

        } else {
            score = Math.max(score - 1, 0);
        }

        repaint();
    }

    private void closeFishingMinigame() {
        boatGame.resetHook();
        fishingFrame.dispose();
        boatGame.resumeGame();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!gameRunning) {
                restartGame();
                gameRunning = true;
                firstJump = true;
                timer.start();
                gameTimer.start();
            }
            barVelocity = jumpStrength;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

    // Metoda do wyświetlania komunikatów zamiast JOptionPane
    private void displayMessage(String message) {
        // Możesz zaimplementować własne okno dialogowe Swing
        // lub wyświetlić komunikat bezpośrednio na panelu minigry
        // Na potrzeby tej zmiany, użyjemy prostego JLabel w nowym oknie
        JFrame messageFrame = new JFrame("Komunikat");
        messageFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        messageFrame.setSize(300, 100);
        messageFrame.setLocationRelativeTo(fishingFrame); // Wyśrodkuj względem okna minigry

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        messageFrame.add(messageLabel);

        messageFrame.setVisible(true);
    }
}
