import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;
import javax.imageio.ImageIO; // Dodany import dla ImageIO
import java.awt.image.BufferedImage; // Dodany import dla BufferedImage

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

    private BufferedImage logoImage; // Zmienna do przechowywania obrazu logo

    // Cloud positioning arrays for random distribution
    private int[] cloudX = new int[8];  // Increased number of clouds
    private int[] cloudY = new int[8];
    private int[] cloudSize = new int[8];

    // Pixel art colors
    private final Color SKY_BLUE = new Color(135, 206, 235);
    private final Color WATER_BLUE = new Color(65, 105, 225);
    private final Color WATER_DARK = new Color(25, 25, 112);
    private final Color BOAT_BROWN = new Color(139, 69, 19);
    private final Color BOAT_DARK = new Color(101, 67, 33);
    private final Color FISH_ORANGE = new Color(255, 140, 0);
    private final Color FISH_RED = new Color(220, 20, 60);
    private final Color SHOP_WOOD = new Color(160, 82, 45);
    private final Color ROPE_COLOR = new Color(101, 67, 33);

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
        initializeClouds();  // Initialize random cloud positions

        // Ładowanie obrazu logo
        try {
            logoImage = ImageIO.read(new File("LOGO.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Błąd ładowania obrazu LOGO.png. Upewnij się, że plik istnieje i jest w odpowiednim katalogu.");
        }

        for (int i = 0; i < fish2Array.length; i++) {
            fish2Array[i] = new Fish2(screenWidth, waterLevel, screenHeight);
        }

        loadFishCaughtFromFile();
        loadMoneyFromFile();
        loadBoatUpgradeLevel();  // Ładujemy poziom ulepszenia łodzi

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

    private void initializeClouds() {
        // Generate random positions for clouds
        for (int i = 0; i < cloudX.length; i++) {
            cloudX[i] = random.nextInt(screenWidth - 100);  // Leave some margin
            cloudY[i] = random.nextInt(waterLevel / 2);     // Only in upper half of sky
            cloudSize[i] = random.nextInt(3) + 1;           // Size variation (1-3)
        }
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
            if (fishY[i] < 0) {
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

    // Pixel art drawing methods
    private void drawPixelBoat(Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g;

        // Main boat body
        g2d.setColor(BOAT_BROWN);
        g2d.fillRect(x, y, width, height);

        // Boat outline
        g2d.setColor(BOAT_DARK);
        g2d.drawRect(x, y, width, height);

        // Boat details
        g2d.setColor(BOAT_DARK);
        g2d.fillRect(x + 2, y + 2, width - 4, 4);
        g2d.fillRect(x + 5, y + height - 8, width - 10, 4);

        // Mast
        g2d.setColor(BOAT_DARK);
        g2d.fillRect(x + width/2 - 2, y - 20, 4, 20);

        // Flag
        g2d.setColor(Color.RED);
        g2d.fillRect(x + width/2 + 2, y - 18, 12, 8);
    }

    private void drawPixelFish(Graphics g, int x, int y, int width, int height, Color fishColor) {
        Graphics2D g2d = (Graphics2D) g;

        // Fish body
        g2d.setColor(fishColor);
        g2d.fillOval(x, y, width - 8, height);

        // Fish tail
        int[] tailX = {x + width - 8, x + width, x + width - 8};
        int[] tailY = {y, y + height/2, y + height};
        g2d.fillPolygon(tailX, tailY, 3);

        // Fish outline
        g2d.setColor(Color.BLACK);
        g2d.drawOval(x, y, width - 8, height);
        g2d.drawPolygon(tailX, tailY, 3);

        // Fish eye
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x + 2, y + 3, 6, 6);
        g2d.setColor(Color.BLACK);
        g2d.fillOval(x + 4, y + 5, 2, 2);

        // Fish details
        g2d.setColor(fishColor.darker());
        g2d.drawLine(x + 3, y + height/2, x + width - 12, y + height/2);
    }

    private void drawPixelWater(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Water gradient effect (reversed - darker at top, lighter at bottom)
        for (int i = 0; i < getHeight() - waterLevel; i += 8) {
            int alpha = Math.min(255, 50 + i/2);  // Reversed gradient
            Color waterColor = new Color(WATER_BLUE.getRed(), WATER_BLUE.getGreen(), WATER_BLUE.getBlue(), alpha);
            g2d.setColor(waterColor);
            g2d.fillRect(0, waterLevel + i, getWidth(), 8);
        }

        // Water surface waves
        g2d.setColor(Color.WHITE);
        for (int x = 0; x < getWidth(); x += 20) {
            g2d.drawLine(x, waterLevel, x + 10, waterLevel + 2);
            g2d.drawLine(x + 10, waterLevel + 2, x + 20, waterLevel);
        }
    }

    private void drawPixelShop(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Shop base
        g2d.setColor(SHOP_WOOD);
        g2d.fillRect(shopArea.x, shopArea.y, shopArea.width, shopArea.height);

        // Shop outline
        g2d.setColor(SHOP_WOOD.darker());
        g2d.drawRect(shopArea.x, shopArea.y, shopArea.width, shopArea.height);

        // Wood planks effect
        g2d.setColor(SHOP_WOOD.darker());
        for (int i = 0; i < shopArea.height; i += 12) {
            g2d.drawLine(shopArea.x, shopArea.y + i, shopArea.x + shopArea.width, shopArea.y + i);
        }

        // Shop roof
        g2d.setColor(new Color(139, 0, 0));
        int[] roofX = {shopArea.x - 10, shopArea.x + shopArea.width/2, shopArea.x + shopArea.width + 10};
        int[] roofY = {shopArea.y, shopArea.y - 20, shopArea.y};
        g2d.fillPolygon(roofX, roofY, 3);
        g2d.setColor(Color.BLACK);
        g2d.drawPolygon(roofX, roofY, 3);

        // Shop sign
        g2d.setColor(Color.WHITE);
        g2d.fillRect(shopArea.x + 10, shopArea.y + 20, shopArea.width - 20, 20);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(shopArea.x + 10, shopArea.y + 20, shopArea.width - 20, 20);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 12));
        g2d.drawString("SKLEP", shopArea.x + 45, shopArea.y + 35);

        // Shop door
        g2d.setColor(SHOP_WOOD.darker());
        g2d.fillRect(shopArea.x + 60, shopArea.y + 45, 30, 55);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(shopArea.x + 60, shopArea.y + 45, 30, 55);

        // Door handle
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(shopArea.x + 82, shopArea.y + 70, 4, 4);
    }

    private void drawPixelHook(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        if (boat.isHookDropped()) {
            // Rope
            g2d.setColor(ROPE_COLOR);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(boat.getHookX(), boat.getBoatY() + boat.getBoatHeight(),
                    boat.getHookX(), boat.getHookY());

            // Hook
            g2d.setColor(Color.GRAY);
            g2d.fillRect(boat.getHookX() - 3, boat.getHookY(), 6, 8);
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawRect(boat.getHookX() - 3, boat.getHookY(), 6, 8);

            // Hook curve
            g2d.drawArc(boat.getHookX() - 5, boat.getHookY() + 6, 10, 6, 0, 180);
        }
    }

    private void drawLogo(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Logo background
        g2d.setColor(new Color(255, 255, 255, 255)); // Białe tło
        g2d.fillRoundRect(10, 10, 600, 80, 10, 10);
        g2d.setColor(new Color(0, 0, 0, 150)); // Delikatna czarna obwódka
        g2d.drawRoundRect(10, 10, 600, 80, 10, 10);

        // University logo emblem - load and scale image
        if (logoImage != null) {
            // Rysuj obraz logo w punkcie (25, 20) i przeskaluj do rozmiaru 60x60
            g2d.drawImage(logoImage, 25, 20, 60, 60, this);
        } else {
            // Jeśli obraz nie został załadowany, narysuj zastępczy prostokąt
            g2d.setColor(new Color(204, 0, 51)); // Czerwony kolor z logo
            g2d.fillRect(25, 20, 60, 60); // Kwadratowe tło dla emblematu
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            g2d.drawString("Logo", 40, 50); // Zastępczy tekst
        }

        // University name (previous text, now preserved)
        g2d.setColor(new Color(50, 50, 50)); // Ciemnoszary kolor dla tekstu
        g2d.setFont(new Font("Arial", Font.BOLD, 22)); // Większa i pogrubiona czcionka
        g2d.drawString("Państwowa Akademia", 90, 45);

        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        g2d.drawString("Nauk Stosowanych", 90, 65);

        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        g2d.drawString("w Krośnie", 90, 85);
    }

    private void drawPixelUI(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // UI Background
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRect(getWidth() - 250, 10, 240, 70);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(getWidth() - 250, 10, 240, 70);

        // Text with pixel font style
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));

        // Fish icon PRZED napisem "Złapane ryby"
        drawPixelFish(g2d, getWidth() - 240, 15, 20, 12, FISH_ORANGE);
        g2d.drawString("Złapane ryby: " + fishCaught, getWidth() - 215, 30);

        // Money icon PRZED napisem "Pieniądze"
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(getWidth() - 240, 35, 12, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(getWidth() - 240, 35, 12, 12);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 8));
        g2d.drawString("$", getWidth() - 236, 44);

        // Powrót do normalnej czcionki dla tekstu pieniędzy
        g2d.setColor(Color.yellow);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2d.drawString("Pieniądze: " + money + " PLN", getWidth() - 215, 50);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable antialiasing for smoother pixel art
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        // Sky
        g2d.setColor(SKY_BLUE);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Random clouds
        g2d.setColor(Color.WHITE);
        for (int i = 0; i < cloudX.length; i++) {
            drawPixelCloud(g2d, cloudX[i], cloudY[i], cloudSize[i]);
        }

        // Water
        drawPixelWater(g2d);

        // Shop
        drawPixelShop(g2d);

        // Boat
        drawPixelBoat(g2d, boat.getBoatX(), boat.getBoatY(), boat.getBoatWidth(), boat.getBoatHeight());

        // Hook
        drawPixelHook(g2d);

        // Fish
        for (int i = 0; i < fishCount; i++) {
            if (fishY[i] >= 0) {
                drawPixelFish(g2d, fishX[i], fishY[i], fishWidth, fishHeight, FISH_ORANGE);
            }
        }

        // Special fish (Fish2)
        for (Fish2 fish2 : fish2Array) {
            if (fish2.isVisible()) {
                fish2.draw(g);
            }
        }

        // UI
        drawPixelUI(g2d);

        // Logo (drawn on top)
        drawLogo(g2d);
    }

    private void drawPixelCloud(Graphics2D g2d, int x, int y, int size) {
        g2d.setColor(Color.WHITE);

        // Base cloud size multiplier
        int baseSize = 25 + (size * 10);  // Size 1=35px, Size 2=45px, Size 3=55px

        // Draw multiple overlapping ovals to create cloud shape
        g2d.fillOval(x, y, baseSize, baseSize * 2/3);
        g2d.fillOval(x + baseSize/3, y - baseSize/6, baseSize * 4/3, baseSize);
        g2d.fillOval(x + baseSize * 2/3, y + baseSize/6, baseSize, baseSize * 2/3);
        g2d.fillOval(x + baseSize/6, y + baseSize/3, baseSize * 5/4, baseSize/2);

        // Add some wispy edges for more natural look
        g2d.fillOval(x - baseSize/6, y + baseSize/4, baseSize/2, baseSize/3);
        g2d.fillOval(x + baseSize, y + baseSize/4, baseSize/2, baseSize/3);
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
                fishY[i] = -fishHeight;
                timer.stop();
                startFishingMinigame(false);
                return;
            }
        }

        for (Fish2 fish2 : fish2Array) {
            if (fish2.isVisible() && hookRect.intersects(fish2.getBounds())) {
                fish2.remove();
                timer.stop();
                caughtRedFish = true;
                break;
            }
        }

        if (caughtRedFish) {
            startFishingMinigame(true);
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

        for (int i = 0; i < fishCount; i++) {
            Rectangle hookRect = new Rectangle(boat.getHookX() - 2, boat.getHookY(), 4, 10);
            Rectangle fishRect = new Rectangle(fishX[i], fishY[i], fishWidth, fishHeight);
            if (hookRect.intersects(fishRect)) {
                fishY[i] = -fishHeight;
                break;
            }
        }

        for (Fish2 fish2 : fish2Array) {
            if (fish2.isVisible() && new Rectangle(boat.getHookX() - 2, boat.getHookY(), 4, 10).intersects(fish2.getBounds())) {
                fish2.remove();
            }
        }

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
            JFrame frame = new JFrame("Boat Fishing Game - Pixel Art Edition");
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