import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/**
 * Implementuje panel minigry wędkarskiej, która jest uruchamiana po złapaniu ryby.
 * Zadaniem gracza jest utrzymanie zielonego paska na poruszającej się w pionie rybie,
 * aby napełnić pasek postępu i wygrać. Minigra posiada własną pętlę, fizykę
 * oraz system punktacji, a jej wynik jest zapisywany do pliku.
 */
public class FishingMinigame extends JPanel implements ActionListener, KeyListener {
    private int barY, fishY;
    private int fishSpeed = 2;
    private int barVelocity = 0;
    /** Siła grawitacji działająca na pasek gracza, ciągnąc go w dół. */
    private int gravity = 1;
    /** Siła, z jaką pasek gracza "skacze" do góry po naciśnięciu spacji. */
    private int jumpStrength = -10;
    private int barHeight = 60;
    private int fishHeight = 40;
    /** Aktualny postęp złapania ryby (0-100). */
    private int score = 0;
    /** Statyczna zmienna przechowująca łączną liczbę złapanych ryb. */
    private static int fishCaught = 0;
    /** Główny timer minigry, odpowiada za aktualizację logiki i fizyki. */
    private Timer timer;
    /** Osobny timer do odliczania czasu pozostałego na ukończenie minigry. */
    private Timer gameTimer;
    private int timeLeft = 10;
    private boolean gameRunning = false;
    private boolean firstJump = false;
    /** Górna granica obszaru gry. */
    private final int FRAME_TOP = 50;
    /** Dolna granica obszaru gry. */
    private final int FRAME_BOTTOM = 350;
    private Random random;
    private static final String SAVE_FILE = "fish_caught.txt";
    private BoatGame boatGame;
    private JFrame fishingFrame;
    /** Flaga określająca, czy łapana ryba jest specjalnym (czerwonym) typem. */
    private boolean isRedFish;

    // Obrazy dla elementów minigry
    private BufferedImage minigameBarImage;
    private BufferedImage minigameFishImage;
    private BufferedImage minigameRedFishImage;

    /**
     * Konstruktor minigry wędkarskiej.
     * @param boatGame Referencja do głównego obiektu gry, potrzebna do wznowienia rozgrywki.
     * @param fishingFrame Ramka (okno), w której wyświetlana jest minigra.
     * @param isRedFish True, jeśli łapana ryba jest rzadszym, czerwonym typem.
     */
    public FishingMinigame(BoatGame boatGame, JFrame fishingFrame, boolean isRedFish) {
        this.boatGame = boatGame;
        this.fishingFrame = fishingFrame;
        this.isRedFish = isRedFish;
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
            minigameBarImage = ImageIO.read(new File("minigame_bar_pixel.png"));
            minigameFishImage = ImageIO.read(new File("minigame_fish_pixel.png"));
            minigameRedFishImage = ImageIO.read(new File("minigame_red_fish_pixel.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Błąd wczytywania obrazów pixel art dla minigry!");
        }
    }

    public static int getFishCaughtStatic() {
        return fishCaught;
    }

    /**
     * Metoda wywoływana przez `gameTimer` co sekundę.
     * Odpowiada za odliczanie czasu i zakończenie gry porażką, gdy czas się skończy.
     */
    private void updateGameTimer() {
        if (gameRunning) {
            timeLeft--;
            if (timeLeft <= 0) {
                gameRunning = false;
                timer.stop();
                gameTimer.stop();
                displayMessage("Czas minął! Nie złapałeś ryby.");
                closeFishingMinigame();
            }
        }
        repaint();
    }

    /**
     * Resetuje stan minigry do wartości początkowych, przygotowując ją do nowej rundy.
     */
    private void restartGame() {
        barY = 120;
        barVelocity = 0;
        firstJump = false;
        fishY = new Random().nextInt(FRAME_BOTTOM - FRAME_TOP - fishHeight) + FRAME_TOP;
        score = 0;
        timeLeft = 10;
        gameRunning = false;
        timer.stop();
        gameTimer.stop();
        requestFocusInWindow();
        repaint();
    }

    /**
     * Zapisuje aktualną liczbę złowionych ryb do pliku.
     */
    private void saveFishCaught() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SAVE_FILE))) {
            writer.write(String.valueOf(fishCaught));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Wczytuje liczbę złowionych ryb z pliku przy starcie minigry.
     */
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

    /**
     * Metoda odpowiedzialna za rysowanie wszystkich elementów minigry.
     * @param g Kontekst graficzny do rysowania.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Ustawienia renderingu zapewniające ostry, "pixelowy" wygląd
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Rysowanie tła
        g.setColor(new Color(173, 216, 230));
        g.fillRect(0, 0, getWidth(), getHeight());

        // Rysowanie ramki
        g.setColor(Color.DARK_GRAY);
        g.fillRect(45, FRAME_TOP, 5, FRAME_BOTTOM - FRAME_TOP);
        g.fillRect(45 + 30 - 5, FRAME_TOP, 5, FRAME_BOTTOM - FRAME_TOP);

        // Rysowanie paska gracza
        if (minigameBarImage != null) {
            g2d.drawImage(minigameBarImage, 50, barY, 20, barHeight, this);
        } else {
            g.setColor(Color.GREEN);
            g.fillRect(50, barY, 20, barHeight);
        }

        // Rysowanie ryby (zwykłej lub czerwonej)
        BufferedImage currentFishImage = isRedFish ? minigameRedFishImage : minigameFishImage;
        if (currentFishImage != null) {
            g2d.drawImage(currentFishImage, 50, fishY, 20, fishHeight, this);
        } else {
            g.setColor(isRedFish ? Color.RED : Color.ORANGE);
            g.fillRect(50, fishY, 20, fishHeight);
        }

        // Rysowanie paska postępu
        g.setColor(Color.GRAY);
        g.fillRect(80, 20, 100, 10);
        g.setColor(Color.BLUE);
        g.fillRect(80, 20, score, 10);

        // Wyświetlanie tekstu
        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospaced", Font.BOLD, 16));
        g.drawString("Pozostały czas: " + timeLeft + "s", 100, 50);
        g.drawString("Złapane ryby: " + fishCaught, 100, 70);

        // Wyświetlanie instrukcji startowej
        if (!firstJump) {
            g.setColor(new Color(255, 255, 0, 150));
            g.fillRect(30, 200, 140, 50);
            g.setColor(Color.BLACK);
            g.drawString("Naciśnij SPACE by zacząć", 40, 230);
        }
    }

    /**
     * Główna pętla logiki minigry, wywoływana przez `timer`.
     * Aktualizuje pozycje ryby i paska, sprawdza kolizje i warunki zwycięstwa.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameRunning) return;

        // Czerwone ryby poruszają się szybciej
        fishY += isRedFish ? fishSpeed * 2 : fishSpeed;

        // Odbijanie się ryby od górnej i dolnej krawędzi
        if (fishY <= FRAME_TOP || fishY >= FRAME_BOTTOM - fishHeight) {
            fishSpeed = -fishSpeed;
        }

        // Losowa zmiana kierunku ryby
        if (random.nextInt(100) < 5) {
            fishSpeed = random.nextBoolean() ? 2 : -2;
        }

        // Symulacja grawitacji dla paska gracza
        if (firstJump) {
            barVelocity += gravity;
            barY += barVelocity;
        }

        // Ograniczenie ruchu paska w pionie
        if (barY > FRAME_BOTTOM - barHeight) {
            barY = FRAME_BOTTOM - barHeight;
            barVelocity = 0;
        } else if (barY < FRAME_TOP) {
            barY = FRAME_TOP;
            barVelocity = 0;
        }

        // Sprawdzanie kolizji paska z rybą i aktualizacja wyniku
        if (new Rectangle(50, barY, 20, barHeight).intersects(new Rectangle(50, fishY, 20, fishHeight))) {
            score = Math.min(score + 1, 100);
            if (score >= 100) { // Warunek zwycięstwa
                gameRunning = false;
                timer.stop();
                gameTimer.stop();
                if (isRedFish) {
                    fishCaught += 2; // Czerwona ryba warta jest 2 punkty
                } else {
                    fishCaught++;
                }
                saveFishCaught();
                displayMessage("Złapałeś rybę!");
                closeFishingMinigame();
            }
        } else {
            score = Math.max(score - 1, 0);
        }

        repaint();
    }

    /**
     * Zamyka okno minigry i wznawia główną grę.
     */
    private void closeFishingMinigame() {
        boatGame.resetHook();
        fishingFrame.dispose();
        boatGame.resumeGame();
    }

    /**
     * Obsługuje naciśnięcia klawiszy przez gracza.
     * Spacja rozpoczyna grę i powoduje "skok" paska.
     */
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
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public boolean isFocusable() {
        return true;
    }

    /**
     * Wyświetla komunikat dla gracza w osobnym, stylizowanym oknie,
     * aby zachować spójność wizualną gry.
     * @param message Wiadomość do wyświetlenia.
     */
    private void displayMessage(String message) {
        JFrame messageFrame = new JFrame("Komunikat");
        messageFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        messageFrame.setSize(300, 100);
        messageFrame.setLocationRelativeTo(fishingFrame);

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        messageFrame.add(messageLabel);

        messageFrame.setVisible(true);
    }
}