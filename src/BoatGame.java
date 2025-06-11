import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * Main class for the Boat Fishing Game.
 * This class handles the primary game logic, rendering, user input, and state management,
 * featuring a distinctive pixel art graphical style.
 */
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
    private boolean[] fishFacingLeft = new boolean[5]; // Śledzi kierunek każdej ryby
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

    private BufferedImage logoImage; // Stores the loaded logo image.

    // Arrays for storing the positions and sizes of procedurally generated clouds.
    private int[] cloudX = new int[8];
    private int[] cloudY = new int[8];
    private int[] cloudSize = new int[8];

    // Defines the color palette for the pixel art graphics.
    private final Color SKY_BLUE = new Color(135, 206, 235);
    private final Color WATER_BLUE = new Color(65, 105, 225);
    private final Color WATER_DARK = new Color(25, 25, 112);
    private final Color BOAT_BROWN = new Color(139, 69, 19);
    private final Color BOAT_DARK = new Color(101, 67, 33);
    private final Color FISH_ORANGE = new Color(255, 140, 0);
    private final Color FISH_RED = new Color(220, 20, 60);
    private final Color SHOP_WOOD = new Color(160, 82, 45);
    private final Color ROPE_COLOR = new Color(101, 67, 33);

    /**
     * Constructor for the BoatGame panel.
     * Initializes game components, loads saved data, sets up timers, and configures the game window.
     * @param frame The main JFrame that will contain this game panel.
     */
    public BoatGame(JFrame frame) {
        this.frame = frame;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = screenSize.width;
        screenHeight = screenSize.height;
        waterLevel = screenHeight / 2;

        boat = new Boat(screenWidth, waterLevel);
        // Sets a lower base speed for the boat for better control.
        boat.setSpeedMultiplier(0.75);
        random = new Random();
        spawnFish();
        initializeClouds();

        // Attempts to load the logo image from the specified file.
        try {
            logoImage = ImageIO.read(new File("LOGO.png"));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading LOGO.png. Make sure the file exists in the correct directory.");
        }

        // Initializes the array of special 'Fish2' objects.
        for (int i = 0; i < fish2Array.length; i++) {
            fish2Array[i] = new Fish2(screenWidth, waterLevel, screenHeight);
        }

        loadFishCaughtFromFile();
        loadMoneyFromFile();
        loadBoatUpgradeLevel();

        timer = new Timer(30, this); // Main game loop timer, fires every 30ms.
        fishSpawnTimer = new Timer(3000, e -> respawnFish()); // Timer for respawning fish every 3 seconds.
        fishSpawnTimer.start();

        setFocusable(true);
        addKeyListener(this);
        timer.start();

        shopArea = new Rectangle(0, waterLevel - shopHeight, shopWidth, shopHeight);
        shop = new Shop(this);

        moneyLabel = new JLabel("Pieniądze: " + money + " PLN", SwingConstants.CENTER);
    }

    /**
     * Initializes the positions and sizes of clouds for a random background effect.
     */
    private void initializeClouds() {
        for (int i = 0; i < cloudX.length; i++) {
            cloudX[i] = random.nextInt(screenWidth - 100);  // Horizontal position with margin.
            cloudY[i] = random.nextInt(waterLevel / 2);     // Vertical position in the upper sky.
            cloudSize[i] = random.nextInt(3) + 1;           // Random size variation (1 to 3).
        }
    }

    /**
     * Loads the total number of caught fish from "fish_caught.txt".
     * If the file doesn't exist or contains invalid data, the count defaults to 0.
     * @return The number of fish caught.
     */
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

    /**
     * Saves the current total of caught fish to "fish_caught.txt".
     */
    private void saveFishCaughtToFile() {
        File file = new File("fish_caught.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(String.valueOf(fishCaught));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the player's money from "money.txt".
     * If the file doesn't exist or is invalid, money defaults to 0.
     */
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

    /**
     * Saves the player's current money to "money.txt".
     */
    private void saveMoneyToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(moneyFile))) {
            writer.write(String.valueOf(money));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the player's money, saves it, and refreshes the on-screen display.
     * @param newMoney The new amount of money.
     */
    public void updateMoneyDisplay(int newMoney) {
        money = newMoney;
        moneyLabel.setText("Pieniądze: " + money + " PLN");
        saveMoneyToFile();
        repaint();
    }

    /**
     * Populates the game with fish at the beginning of a session, assigning random positions and speeds.
     */
    private void spawnFish() {
        int minFishY = waterLevel + 50; // Ensures fish spawn below the water surface.
        for (int i = 0; i < fishCount; i++) {
            fishX[i] = random.nextInt(screenWidth - fishWidth);
            fishY[i] = random.nextInt(screenHeight - minFishY - fishHeight) + minFishY;
            fishSpeed[i] = random.nextBoolean() ? 2 : -2;
            fishFacingLeft[i] = fishSpeed[i] < 0; // Ustawia początkowy kierunek
        }
    }

    /**
     * Respawns fish that have been caught and replenishes the special 'Fish2' types.
     * This method is called periodically by the fishSpawnTimer.
     */
    private void respawnFish() {
        int minFishY = waterLevel + 50; // Ensures fish respawn below the water surface.
        for (int i = 0; i < fishCount; i++) {
            if (fishY[i] < 0) { // A negative Y position indicates the fish was caught.
                fishX[i] = random.nextInt(screenWidth - fishWidth);
                fishY[i] = random.nextInt(screenHeight - minFishY - fishHeight) + minFishY;
                fishSpeed[i] = random.nextBoolean() ? 2 : -2;
                fishFacingLeft[i] = fishSpeed[i] < 0; // Ustawia kierunek dla nowej ryby
            }
        }

        for (int i = 0; i < fish2Array.length; i++) {
            if (!fish2Array[i].isVisible()) {
                fish2Array[i] = new Fish2(screenWidth, waterLevel, screenHeight);
            }
        }
    }

    // --- PIXEL ART DRAWING METHODS --- //

    /**
     * Renders a boat using simple geometric shapes to create a pixel art style.
     */
    private void drawPixelBoat(Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(BOAT_BROWN);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(BOAT_DARK);
        g2d.drawRect(x, y, width, height);
        g2d.fillRect(x + 2, y + 2, width - 4, 4);
        g2d.fillRect(x + 5, y + height - 8, width - 10, 4);
        g2d.fillRect(x + width/2 - 2, y - 20, 4, 20);
        g2d.setColor(Color.RED);
        g2d.fillRect(x + width/2 + 2, y - 18, 12, 8);
    }

    /**
     * Renders a fish using ovals and polygons for a pixel art appearance.
     */
    private void drawPixelFish(Graphics g, int x, int y, int width, int height, Color fishColor, boolean facingLeft) {
        Graphics2D g2d = (Graphics2D) g.create(); // Użyj kopii, aby transformacje nie wpływały na inne elementy
        try {
            // Domyślny obrazek ryby jest skierowany w lewo.
            // Odwracamy go (aby był skierowany w prawo) tylko wtedy, gdy facingLeft jest fałszywe.
            if (!facingLeft) {
                // Odwróć grafikę w poziomie
                g2d.translate(x + width, y);
                g2d.scale(-1, 1);
                g2d.translate(-x, -y);
            }

            // Poniższy kod rysuje rybę skierowaną w lewo (ogon po prawej, oko po lewej)
            g2d.setColor(fishColor);
            g2d.fillOval(x, y, width - 8, height); // Ciało
            int[] tailX = {x + width - 8, x + width, x + width - 8}; // Ogon
            int[] tailY = {y, y + height/2, y + height};
            g2d.fillPolygon(tailX, tailY, 3);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(x, y, width - 8, height);
            g2d.drawPolygon(tailX, tailY, 3);
            g2d.setColor(Color.WHITE);
            g2d.fillOval(x + 2, y + 3, 6, 6); // Oko
            g2d.setColor(Color.BLACK);
            g2d.fillOval(x + 4, y + 5, 2, 2);
            g2d.setColor(fishColor.darker());
            g2d.drawLine(x + 3, y + height/2, x + width - 12, y + height/2);
        } finally {
            g2d.dispose(); // Zwolnij zasoby kopii
        }
    }

    /**
     * Renders the water with a gradient effect and surface waves.
     */
    private void drawPixelWater(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        for (int i = 0; i < getHeight() - waterLevel; i += 8) {
            int alpha = Math.min(255, 50 + i/2);
            Color waterColor = new Color(WATER_BLUE.getRed(), WATER_BLUE.getGreen(), WATER_BLUE.getBlue(), alpha);
            g2d.setColor(waterColor);
            g2d.fillRect(0, waterLevel + i, getWidth(), 8);
        }
        g2d.setColor(Color.WHITE);
        for (int x = 0; x < getWidth(); x += 20) {
            g2d.drawLine(x, waterLevel, x + 10, waterLevel + 2);
            g2d.drawLine(x + 10, waterLevel + 2, x + 20, waterLevel);
        }
    }

    /**
     * Renders the shop building on the left side of the screen.
     */
    private void drawPixelShop(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(SHOP_WOOD);
        g2d.fillRect(shopArea.x, shopArea.y, shopArea.width, shopArea.height);
        g2d.setColor(SHOP_WOOD.darker());
        g2d.drawRect(shopArea.x, shopArea.y, shopArea.width, shopArea.height);
        for (int i = 0; i < shopArea.height; i += 12) {
            g2d.drawLine(shopArea.x, shopArea.y + i, shopArea.x + shopArea.width, shopArea.y + i);
        }
        g2d.setColor(new Color(139, 0, 0));
        int[] roofX = {shopArea.x - 10, shopArea.x + shopArea.width/2, shopArea.x + shopArea.width + 10};
        int[] roofY = {shopArea.y, shopArea.y - 20, shopArea.y};
        g2d.fillPolygon(roofX, roofY, 3);
        g2d.setColor(Color.BLACK);
        g2d.drawPolygon(roofX, roofY, 3);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(shopArea.x + 10, shopArea.y + 20, shopArea.width - 20, 20);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(shopArea.x + 10, shopArea.y + 20, shopArea.width - 20, 20);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 12));
        g2d.drawString("SKLEP", shopArea.x + 45, shopArea.y + 35);
        g2d.setColor(SHOP_WOOD.darker());
        g2d.fillRect(shopArea.x + 60, shopArea.y + 45, 30, 55);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(shopArea.x + 60, shopArea.y + 45, 30, 55);
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(shopArea.x + 82, shopArea.y + 70, 4, 4);
    }

    /**
     * Renders the fishing line and hook when it is dropped.
     */
    private void drawPixelHook(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        if (boat.isHookDropped()) {
            g2d.setColor(ROPE_COLOR);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(boat.getHookX(), boat.getBoatY() + boat.getBoatHeight(),
                    boat.getHookX(), boat.getHookY());
            g2d.setColor(Color.GRAY);
            g2d.fillRect(boat.getHookX() - 3, boat.getHookY(), 6, 8);
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawRect(boat.getHookX() - 3, boat.getHookY(), 6, 8);
            g2d.drawArc(boat.getHookX() - 5, boat.getHookY() + 6, 10, 6, 0, 180);
        }
    }

    /**
     * Renders the university logo at the top-left corner of the screen.
     */
    private void drawLogo(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(255, 255, 255, 255));
        g2d.fillRoundRect(10, 10, 600, 80, 10, 10);
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.drawRoundRect(10, 10, 600, 80, 10, 10);

        if (logoImage != null) {
            // Draw the loaded logo image, scaled to fit.
            g2d.drawImage(logoImage, 25, 20, 60, 60, this);
        } else {
            // Draw a placeholder if the image failed to load.
            g2d.setColor(new Color(204, 0, 51));
            g2d.fillRect(25, 20, 60, 60);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            g2d.drawString("Logo", 40, 50);
        }

        g2d.setColor(new Color(50, 50, 50));
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        g2d.drawString("Państwowa Akademia", 90, 45);
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        g2d.drawString("Nauk Stosowanych", 90, 65);
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        g2d.drawString("w Krośnie", 90, 85);
    }

    /**
     * Renders the user interface (UI) panel, showing fish count and money.
     */
    private void drawPixelUI(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRect(getWidth() - 250, 10, 240, 70);
        g2d.setColor(Color.YELLOW);
        g2d.drawRect(getWidth() - 250, 10, 240, 70);
        g2d.setColor(Color.ORANGE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));

        // Draw the fish icon next to the fish count.
        // Domyślnie skierowana w lewo, co pasuje do ułożenia obok tekstu.
        drawPixelFish(g, getWidth() - 240, 15, 20, 12, FISH_ORANGE, true);
        g2d.drawString("Złapane ryby: " + fishCaught, getWidth() - 215, 30);

        // Draw the money icon next to the money display.
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(getWidth() - 240, 35, 12, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(getWidth() - 240, 35, 12, 12);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 8));
        g2d.drawString("$", getWidth() - 236, 44);

        // Set the font and color for the money text.
        g2d.setColor(Color.yellow);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2d.drawString("Pieniądze: " + money + " PLN", getWidth() - 215, 50);
    }

    /**
     * The main rendering method, called by Swing to draw the entire game scene.
     * It orchestrates all the individual drawing methods in the correct order.
     * @param g The Graphics context provided by Swing for drawing.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Disable anti-aliasing to maintain the crisp pixel art aesthetic.
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        // Render all visual layers in order from back to front.
        g2d.setColor(SKY_BLUE);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(Color.WHITE);
        for (int i = 0; i < cloudX.length; i++) {
            drawPixelCloud(g2d, cloudX[i], cloudY[i], cloudSize[i]);
        }

        drawPixelWater(g);
        drawPixelShop(g);
        drawPixelBoat(g2d, boat.getBoatX(), boat.getBoatY(), boat.getBoatWidth(), boat.getBoatHeight());
        drawPixelHook(g);

        for (int i = 0; i < fishCount; i++) {
            if (fishY[i] >= 0) {
                drawPixelFish(g, fishX[i], fishY[i], fishWidth, fishHeight, FISH_ORANGE, fishFacingLeft[i]);
            }
        }

        for (Fish2 fish2 : fish2Array) {
            if (fish2.isVisible()) {
                fish2.draw(g);
            }
        }

        drawPixelUI(g2d);
        drawLogo(g2d);
    }

    /**
     * Renders a single, multi-part cloud shape.
     */
    private void drawPixelCloud(Graphics2D g2d, int x, int y, int size) {
        g2d.setColor(Color.WHITE);
        int baseSize = 25 + (size * 10);
        g2d.fillOval(x, y, baseSize, baseSize * 2/3);
        g2d.fillOval(x + baseSize/3, y - baseSize/6, baseSize * 4/3, baseSize);
        g2d.fillOval(x + baseSize * 2/3, y + baseSize/6, baseSize, baseSize * 2/3);
        g2d.fillOval(x + baseSize/6, y + baseSize/3, baseSize * 5/4, baseSize/2);
        g2d.fillOval(x - baseSize/6, y + baseSize/4, baseSize/2, baseSize/3);
        g2d.fillOval(x + baseSize, y + baseSize/4, baseSize/2, baseSize/3);
    }

    /**
     * The main game loop logic, executed on each tick of the main timer.
     * It handles object movement, collision detection, and game state updates.
     * @param e The ActionEvent triggered by the timer.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (gamePaused) return;

        boat.updateHook(getHeight());

        Rectangle hookRect = new Rectangle(boat.getHookX() - 2, boat.getHookY(), 4, 10);
        Rectangle boatRect = new Rectangle(boat.getBoatX(), boat.getBoatY(), boat.getBoatWidth(), boat.getBoatHeight());

        boolean caughtRedFish = false;

        // Check for collision between the hook and regular fish.
        for (int i = 0; i < fishCount; i++) {
            Rectangle fishRect = new Rectangle(fishX[i], fishY[i], fishWidth, fishHeight);
            if (hookRect.intersects(fishRect)) {
                fishY[i] = -fishHeight;
                timer.stop();
                startFishingMinigame(false);
                return;
            }
        }

        // Check for collision between the hook and special fish.
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

        // Update fish positions and handle bouncing off screen edges.
        for (int i = 0; i < fishCount; i++) {
            if (fishY[i] >= 0) {
                fishX[i] += fishSpeed[i];
                if (fishX[i] <= 0) {
                    fishX[i] = 0; // Prevent fish from going off-screen left.
                    fishSpeed[i] = -fishSpeed[i];
                } else if (fishX[i] >= screenWidth - fishWidth) {
                    fishX[i] = screenWidth - fishWidth; // Prevent fish from going off-screen right.
                    fishSpeed[i] = -fishSpeed[i];
                }
                if (random.nextInt(100) < 5) { // Occasionally, fish change direction randomly.
                    fishSpeed[i] = random.nextBoolean() ? 2 : -2;
                }
                fishFacingLeft[i] = fishSpeed[i] < 0; // Zaktualizuj kierunek na podstawie prędkości
            }
        }

        // Move special fish.
        for (Fish2 fish2 : fish2Array) {
            if (fish2.isVisible()) {
                fish2.move();
            }
        }

        // Check if the player is trying to enter the shop.
        if (shopArea.intersects(boatRect) && !shopOpen && upArrowPressed) {
            shop.setVisible(true);
            shopOpen = true;
            upArrowPressed = false;
            shop.updateFishCount(fishCaught);
        }

        repaint();
    }

    /**
     * Pauses the main game and launches the fishing minigame in a new window.
     * @param isRedFish True if the caught fish is a special 'red' fish, affecting the minigame.
     */
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

    /**
     * Resumes the game after the fishing minigame or shop is closed.
     * Reloads game state from files and restarts necessary timers.
     */
    public void resumeGame() {
        gamePaused = false;
        boat.resetHook();
        loadFishCaughtFromFile();
        loadMoneyFromFile();
        loadBoatUpgradeLevel();
        timer.start();
        fishSpawnTimer.restart();
        repaint();
        shopOpen = false;
    }

    /**
     * Resets the hook to its initial position.
     */
    public void resetHook() {
        boat.resetHook();
    }

    /**
     * Moves the boat away from the shop area to prevent re-entering immediately.
     */
    public void moveBoatAwayFromShop() {
        boat.setBoatX(shopArea.width + 5);
        repaint();
        shopOpen = false;
    }

    /**
     * Handles key press events for controlling the boat and game actions.
     * @param e The KeyEvent generated by the key press.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT) {
            boat.moveLeft();
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            boat.moveRight();
        } else if (keyCode == KeyEvent.VK_SPACE) {
            if (boat.isHookDropped()) {
                boat.resetHook();  // Retract the hook if it's already down.
            } else {
                boat.dropHook();   // Drop the hook.
            }
        } else if (keyCode == KeyEvent.VK_UP) {
            upArrowPressed = true; // Flag that the up arrow was pressed (for entering the shop).
        }
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {} // Not used.
    @Override
    public void keyTyped(KeyEvent e) {} // Not used.

    /**
     * Main entry point for the application.
     * Creates the main window (JFrame) and initializes the game panel.
     * @param args Command line arguments (not used).
     */
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

    /**
     * Updates the count of caught fish, saves it to a file, and repaints the screen.
     * @param fishCaught The new total number of caught fish.
     */
    public void updateFishCaught(int fishCaught) {
        this.fishCaught = fishCaught;
        saveFishCaughtToFile();
        repaint();
    }

    /**
     * Returns the main boat object.
     * @return The game's Boat instance.
     */
    public Boat getBoat() {
        return boat;
    }

    /**
     * Loads the boat's current upgrade level from "boat_upgrade.txt".
     * Defaults to level 0 if the file is not found or invalid.
     */
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

    /**
     * Saves the boat's current upgrade level to "boat_upgrade.txt".
     */
    public void saveBoatUpgradeLevel() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(boatUpgradeFile))) {
            writer.write(String.valueOf(boat.getBoatUpgradeLevel()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}