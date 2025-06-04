import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;
import java.awt.image.BufferedImage; // Import dla obrazów
import javax.imageio.ImageIO; // Import dla wczytywania obrazów

public class BoatGame extends JPanel implements ActionListener, KeyListener {
    private Boat boat;
    private Timer timer;
    private Timer fishSpawnTimer;
    private Random random;
    private int[] fishX = new int[5];
    private int[] fishY = new int[5];
    private final int fishWidth = 30;
    private final int fishHeight = 20;
    private int[] fishSpeed = new int[5];
    private JFrame frame;
    private int screenWidth, screenHeight;
    private int waterLevel;
    private boolean gamePaused = false;
    private final int fishCount = 5;
    private final int shopWidth = 150;
    private final int shopHeight = 100;
    private Rectangle shopArea;
    private Fish2[] fish2Array = new Fish2[2];
    private int fishCaught = 0;
    private int money = 0;
    private Shop shop;
    private boolean shopOpen = false;
    private final File moneyFile = new File("money.txt");
    private JLabel moneyLabel;
    private boolean upArrowPressed = false;
    private final File boatUpgradeFile = new File("boat_upgrade.txt");

    // Obrazki pixel art
    private BufferedImage boatImage;
    private BufferedImage normalFishImage;
    private BufferedImage redFishImage; // Dla Fish2
    private BufferedImage hookImage;
    private BufferedImage skyImage;
    private BufferedImage waterImage;
    private BufferedImage shopAreaImage;

    public BoatGame(JFrame frame) {
        this.frame = frame;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = screenSize.width;
        screenHeight = screenSize.height;
        waterLevel = screenHeight / 2;

        boat = new Boat(screenWidth, waterLevel);
        // Ustawiamy bazową prędkość łodzi na mniejszą wartość
        boat.setSpeedMultiplier(0.75);
        random = new Random();
        spawnFish();

        for (int i = 0; i < fish2Array.length; i++) {
            fish2Array[i] = new Fish2(screenWidth, waterLevel, screenHeight);
        }

        loadFishCaughtFromFile();
        loadMoneyFromFile();
        loadBoatUpgradeLevel();  // Ładujemy poziom ulepszenia łodzi

        // Wczytywanie obrazków pixel art
        try {
            boatImage = ImageIO.read(new File("boat_pixel.png"));
            normalFishImage = ImageIO.read(new File("fish_orange_pixel.png"));
            redFishImage = ImageIO.read(new File("fish_red_pixel.png")); // Obrazek dla Fish2
            hookImage = ImageIO.read(new File("hook_pixel.png"));
            skyImage = ImageIO.read(new File("background_sky_pixel.png")); // Obrazek dla nieba
            waterImage = ImageIO.read(new File("background_water_pixel.png")); // Obrazek dla wody
            shopAreaImage = ImageIO.read(new File("shop_area_pixel.png")); // Obrazek dla obszaru sklepu
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Błąd wczytywania obrazów pixel art! Upewnij się, że pliki są w katalogu głównym lub podaj pełną ścieżkę.");
        }

        timer = new Timer(30, this);
        fishSpawnTimer = new Timer(3000, e -> respawnFish());
        fishSpawnTimer.start();

        setFocusable(true);
        addKeyListener(this);
        timer.start();

        shopArea = new Rectangle(0, waterLevel - shopHeight, shopWidth, shopHeight);
        shop = new Shop(this);

        moneyLabel = new JLabel("Pieniądze: " + money + " PLN", SwingConstants.CENTER);
    }

    private int loadFishCaughtFromFile() {
        File file = new File("fish_caught.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null) {
                fishCaught = Integer.parseInt(line.trim());
            } else {
                fishCaught = 0;
            }
        } catch (IOException | NumberFormatException e) {
            fishCaught = 0;
            e.printStackTrace();
        }
        return fishCaught;
    }

    private void saveFishCaughtToFile() {
        File file = new File("fish_caught.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(String.valueOf(fishCaught));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMoneyFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(moneyFile))) {
            String line = reader.readLine();
            if (line != null) {
                money = Integer.parseInt(line.trim());
            }
        } catch (IOException | NumberFormatException e) {
            money = 0;
        }
    }

    private void saveMoneyToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(moneyFile))) {
            writer.write(String.valueOf(money));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateMoneyDisplay(int newMoney) {
        money = newMoney;
        moneyLabel.setText("Pieniądze: " + money + " PLN");
        saveMoneyToFile();
        repaint();
    }

    private void spawnFish() {
        int minFishY = waterLevel + 50; // Minimalna odległość od tafli wody (np. 50 pikseli)
        for (int i = 0; i < fishCount; i++) {
            fishX[i] = random.nextInt(screenWidth - fishWidth);
            fishY[i] = random.nextInt(screenHeight - minFishY - fishHeight) + minFishY;
            fishSpeed[i] = random.nextBoolean() ? 2 : -2;
        }
    }

    private void respawnFish() {
        int minFishY = waterLevel + 50; // Minimalna odległość od tafli wody (np. 50 pikseli)
        for (int i = 0; i < fishCount; i++) {
            // Jeśli ryba jest poza ekranem (została złapana lub zniknęła)
            if (fishY[i] < 0 || fishY[i] > screenHeight) { // Dodano warunek dla ryb, które mogłyby zniknąć w dół
                fishX[i] = random.nextInt(screenWidth - fishWidth);
                fishY[i] = random.nextInt(screenHeight - minFishY - fishHeight) + minFishY;
                fishSpeed[i] = random.nextBoolean() ? 2 : -2;
            }
        }

        for (int i = 0; i < fish2Array.length; i++) {
            if (!fish2Array[i].isVisible()) {
                fish2Array[i] = new Fish2(screenWidth, waterLevel, screenHeight);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Ustawienia dla rysowania pixel art (wyłączenie antyaliasingu i interpolacji)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // Rysowanie tła (niebo i woda)
        if (skyImage != null && waterImage != null) {
            g2d.drawImage(skyImage, 0, 0, screenWidth, waterLevel, this);
            g2d.drawImage(waterImage, 0, waterLevel, screenWidth, screenHeight - waterLevel, this);
        } else {
            // Domyślne rysowanie, jeśli obrazki tła nie zostały wczytane
            g.setColor(new Color(135, 206, 235)); // Jasny niebieski
            g.fillRect(0, 0, getWidth(), waterLevel);
            g.setColor(new Color(0, 191, 255)); // Głęboki błękit
            g.fillRect(0, waterLevel, getWidth(), getHeight() - waterLevel);
        }

        // Rysowanie obszaru sklepu
        if (shopAreaImage != null) {
            g2d.drawImage(shopAreaImage, shopArea.x, shopArea.y, shopArea.width, shopArea.height, this);
        } else {
            // Domyślne rysowanie, jeśli obrazek sklepu nie został wczytany
            g.setColor(new Color(139, 69, 19)); // Brązowy dla sklepu
            g.fillRect(shopArea.x, shopArea.y, shopArea.width, shopArea.height);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14)); // Mniejsza czcionka dla pixel art
            g.drawString("SKLEP", shopArea.x + 40, shopArea.y + 50);
        }

        // Rysowanie łodzi
        if (boatImage != null) {
            g2d.drawImage(boatImage, boat.getBoatX(), boat.getBoatY(), boat.getBoatWidth(), boat.getBoatHeight(), this);
        } else {
            g.setColor(Color.RED);
            g.fillRect(boat.getBoatX(), boat.getBoatY(), boat.getBoatWidth(), boat.getBoatHeight());
        }

        // Rysowanie haka i linki
        g.setColor(Color.BLACK); // Linka haka
        if (boat.isHookDropped()) {
            g2d.drawLine(boat.getHookX(), boat.getBoatY() + boat.getBoatHeight(), boat.getHookX(), boat.getHookY());
            if (hookImage != null) {
                g2d.drawImage(hookImage, boat.getHookX() - 5, boat.getHookY(), 10, 20, this); // Dopasuj rozmiar haka
            } else {
                g.fillRect(boat.getHookX() - 2, boat.getHookY(), 4, 10); // Domyślny hak
            }
        }

        // Rysowanie standardowych ryb
        if (normalFishImage != null) {
            for (int i = 0; i < fishCount; i++) {
                if (fishY[i] >= 0) { // Tylko jeśli ryba jest widoczna
                    g2d.drawImage(normalFishImage, fishX[i], fishY[i], fishWidth, fishHeight, this);
                }
            }
        } else {
            for (int i = 0; i < fishCount; i++) {
                if (fishY[i] >= 0) {
                    g.setColor(Color.ORANGE);
                    g.fillOval(fishX[i], fishY[i], fishWidth, fishHeight);
                }
            }
        }

        // Rysowanie ryb Fish2 (czerwonych)
        // Fish2 ma własną metodę draw, która używa obrazka
        for (Fish2 fish2 : fish2Array) {
            if (fish2.isVisible()) {
                fish2.draw(g2d); // Używamy metody draw z klasy Fish2
            }
        }

        // Wyświetlanie tekstu (ryby złapane, pieniądze)
        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospaced", Font.BOLD, 18)); // Czcionka stylizowana na pixel art
        g.drawString("Złapane ryby: " + fishCaught, getWidth() - 220, 30);
        g.drawString("Pieniądze: " + money + " PLN", getWidth() - 220, 50);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gamePaused) return;

        boat.updateHook(getHeight());

        Rectangle hookRect = new Rectangle(boat.getHookX() - 2, boat.getHookY(), 4, 10);
        Rectangle boatRect = new Rectangle(boat.getBoatX(), boat.getBoatY(), boat.getBoatWidth(), boat.getBoatHeight());

        boolean caughtRedFish = false;

        for (int i = 0; i < fishCount; i++) {
            Rectangle fishRect = new Rectangle(fishX[i], fishY[i], fishWidth, fishHeight);
            if (hookRect.intersects(fishRect)) {
                fishY[i] = -fishHeight; // Usuń rybę z ekranu
                timer.stop();
                startFishingMinigame(false); // Standardowa ryba
                return;
            }
        }

        for (Fish2 fish2 : fish2Array) {
            if (fish2.isVisible() && hookRect.intersects(fish2.getBounds())) {
                fish2.remove(); // Usuń czerwoną rybę
                timer.stop();
                caughtRedFish = true;
                break;
            }
        }

        if (caughtRedFish) {
            startFishingMinigame(true); // Czerwona ryba
            return;
        }

        for (int i = 0; i < fishCount; i++) {
            if (fishY[i] >= 0) {
                fishX[i] += fishSpeed[i];
                // Zmieniamy warunki odbijania ryb
                if (fishX[i] <= 0) {
                    fishX[i] = 0; // Upewniamy się, że ryba nie wyjdzie poza lewą krawędź
                    fishSpeed[i] = -fishSpeed[i];
                } else if (fishX[i] >= screenWidth - fishWidth) {
                    fishX[i] = screenWidth - fishWidth; // Upewniamy się, że ryba nie wyjdzie poza prawą krawędź
                    fishSpeed[i] = -fishSpeed[i];
                }
                if (random.nextInt(100) < 5) {
                    fishSpeed[i] = random.nextBoolean() ? 2 : -2;
                }
            }
        }

        for (Fish2 fish2 : fish2Array) {
            if (fish2.isVisible()) {
                fish2.move();
            }
        }

        if (shopArea.intersects(boatRect) && !shopOpen && upArrowPressed) {
            shop.setVisible(true);
            shopOpen = true;
            upArrowPressed = false;
            shop.updateFishCount(fishCaught);  // Update fish count in shop
        }

        repaint();
    }

    private void startFishingMinigame(boolean isRedFish) {
        gamePaused = true;

        // Upewnij się, że złapana ryba jest usunięta z głównej gry przed rozpoczęciem minigry
        // Ten fragment kodu był już w actionPerformed, ale dla pewności powtarzam go tutaj
        // aby upewnić się, że ryba jest "usunięta" przed uruchomieniem minigry.
        // W FishingMinigame i tak jest logiczne usuwanie ryby.

        if (!fishSpawnTimer.isRunning()) {
            fishSpawnTimer.restart();
        }

        SwingUtilities.invokeLater(() -> {
            JFrame fishingFrame = new JFrame("Fishing Minigame");
            FishingMinigame fishingMinigame = new FishingMinigame(this, fishingFrame, isRedFish);
            fishingFrame.add(fishingMinigame);
            fishingFrame.setSize(250, 500);
            fishingFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            fishingFrame.setVisible(true);
            fishingMinigame.requestFocusInWindow();
        });
    }

    public void resumeGame() {
        gamePaused = false;
        boat.resetHook();
        loadFishCaughtFromFile();
        loadMoneyFromFile();
        loadBoatUpgradeLevel();  // Load boat upgrade level
        timer.start();
        fishSpawnTimer.restart();
        repaint();
        shopOpen = false;
    }

    public void resetHook() {
        boat.resetHook();
    }

    public void moveBoatAwayFromShop() {
        boat.setBoatX(shopArea.width + 5);
        repaint();
        shopOpen = false;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            boat.moveLeft();
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            boat.moveRight();
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (boat.isHookDropped()) {
                boat.resetHook();  // Retract the hook
            } else {
                boat.dropHook();   // Drop the hook
            }
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            upArrowPressed = true;
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Boat Fishing Game");
            frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            BoatGame game = new BoatGame(frame);
            frame.add(game);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            game.requestFocusInWindow();
        });
    }

    public void updateFishCaught(int fishCaught) {
        this.fishCaught = fishCaught;
        saveFishCaughtToFile();
        repaint();
    }

    public Boat getBoat() {
        return boat;
    }

    private void loadBoatUpgradeLevel() {
        try (BufferedReader reader = new BufferedReader(new FileReader(boatUpgradeFile))) {
            String line = reader.readLine();
            if (line != null) {
                boat.setBoatUpgradeLevel(Integer.parseInt(line.trim()));
            } else {
                boat.setBoatUpgradeLevel(0);
            }
        } catch (IOException | NumberFormatException e) {
            boat.setBoatUpgradeLevel(0);
        }
    }

    public void saveBoatUpgradeLevel() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(boatUpgradeFile))) {
            writer.write(String.valueOf(boat.getBoatUpgradeLevel()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
