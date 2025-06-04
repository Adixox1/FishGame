import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
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
    private int money = 0;    private Shop shop;
    private boolean shopOpen = false;
    private final File moneyFile = new File("money.txt");
    private JLabel moneyLabel;
    private boolean upArrowPressed = false;
    private final File boatUpgradeFile = new File("boat_upgrade.txt");
    
    // Parallax variables
    private float skyOffsetX = 0;
    private float waterOffsetX = 0;
    private final float parallaxSpeed = 0.5f;
    
    // Responsive controls
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private final float boatAcceleration = 0.8f;

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
        loadBoatUpgradeLevel();  // Ładujemy poziom ulepszenia łodzi        // Wczytywanie obrazków pixel art - używamy wbudowanej grafiki pixel art
        try {
            boatImage = createPixelArtBoat();
            normalFishImage = createPixelArtFish(false);
            redFishImage = createPixelArtFish(true);
            hookImage = createPixelArtHook();
            skyImage = createPixelArtSky();
            waterImage = createPixelArtWater();
            shopAreaImage = createPixelArtShop();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Błąd tworzenia grafiki pixel art!");
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
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);        // Rysowanie tła (niebo i woda) - tile pattern dla pixel art z paralaksą
        if (skyImage != null && waterImage != null) {
            // Rysowanie nieba z paralaksą (wolniejsze przesuwanie)
            int skyImageWidth = skyImage.getWidth();
            int skyImageHeight = skyImage.getHeight();
            
            for (int x = (int)skyOffsetX - skyImageWidth; x < screenWidth + skyImageWidth; x += skyImageWidth) {
                for (int y = 0; y < waterLevel; y += skyImageHeight) {
                    if (x + skyImageWidth > 0 && x < screenWidth) {
                        g2d.drawImage(skyImage, x, y, 
                                     Math.min(skyImageWidth, screenWidth - x), 
                                     Math.min(skyImageHeight, waterLevel - y), this);
                    }
                }
            }
            
            // Rysowanie wody z paralaksą (szybsze przesuwanie)
            int waterImageWidth = waterImage.getWidth();
            int waterImageHeight = waterImage.getHeight();
            
            for (int x = (int)waterOffsetX - waterImageWidth; x < screenWidth + waterImageWidth; x += waterImageWidth) {
                for (int y = waterLevel; y < screenHeight; y += waterImageHeight) {
                    if (x + waterImageWidth > 0 && x < screenWidth) {
                        g2d.drawImage(waterImage, x, y, 
                                     Math.min(waterImageWidth, screenWidth - x),
                                     Math.min(waterImageHeight, screenHeight - y), this);
                    }
                }
            }
        } else {
            // Domyślne rysowanie pixel art style z gradientem
            // Niebo z gradientem
            for (int y = 0; y < waterLevel; y++) {
                float ratio = (float)y / waterLevel;
                int blue = (int)(235 + ratio * 20);
                g.setColor(new Color(135, 206, Math.min(255, blue)));
                g.fillRect(0, y, getWidth(), 1);
            }
            
            // Woda z falami paralaks
            g.setColor(new Color(0, 119, 190));
            g.fillRect(0, waterLevel, getWidth(), getHeight() - waterLevel);
            
            // Animowane fale
            g.setColor(new Color(0, 149, 220));
            for (int y = waterLevel + 5; y < getHeight(); y += 15) {
                for (int x = (int)waterOffsetX; x < getWidth() + 20; x += 20) {
                    g.fillRect(x % getWidth(), y, 8, 2);
                }
            }
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
        }        // Rysowanie łodzi z możliwością odbicia poziomego
        if (boatImage != null) {
            // Check if boat should be mirrored
            if (boat.isFacingLeft()) {
                // Save current transform
                AffineTransform oldTransform = g2d.getTransform();
                
                // Create flip transform around boat center
                AffineTransform flipTransform = new AffineTransform();
                flipTransform.translate(boat.getBoatX() + boat.getBoatWidth(), boat.getBoatY());
                flipTransform.scale(-1, 1); // Flip horizontally
                flipTransform.translate(-boat.getBoatWidth(), 0);
                
                g2d.setTransform(flipTransform);
                g2d.drawImage(boatImage, 0, 0, boat.getBoatWidth(), boat.getBoatHeight(), this);
                
                // Restore original transform
                g2d.setTransform(oldTransform);
            } else {
                // Draw normally (facing right)
                g2d.drawImage(boatImage, boat.getBoatX(), boat.getBoatY(), boat.getBoatWidth(), boat.getBoatHeight(), this);
            }
        } else {
            g.setColor(Color.RED);
            g.fillRect(boat.getBoatX(), boat.getBoatY(), boat.getBoatWidth(), boat.getBoatHeight());
        }        // Rysowanie haka i linki z możliwością odbicia poziomego
        g.setColor(Color.BLACK); // Linka haka
        if (boat.isHookDropped()) {
            // Calculate fishing line attachment point based on boat direction
            int lineAttachX = boat.isFacingLeft() ? 
                boat.getBoatX() + (int)(boat.getBoatWidth() * 0.25) : // Left side attachment when facing left
                boat.getBoatX() + (int)(boat.getBoatWidth() * 0.75);  // Right side attachment when facing right
            
            g2d.drawLine(lineAttachX, boat.getBoatY() + boat.getBoatHeight(), boat.getHookX(), boat.getHookY());
            
            // Draw hook with potential mirroring
            if (hookImage != null) {
                if (boat.isFacingLeft()) {
                    // Save current transform for hook mirroring
                    AffineTransform oldTransform = g2d.getTransform();
                    
                    // Create flip transform for hook
                    AffineTransform flipTransform = new AffineTransform();
                    flipTransform.translate(boat.getHookX() + 5, boat.getHookY());
                    flipTransform.scale(-1, 1); // Flip horizontally
                    flipTransform.translate(-10, 0);
                    
                    g2d.setTransform(flipTransform);
                    g2d.drawImage(hookImage, 0, 0, 10, 20, this);
                    
                    // Restore original transform
                    g2d.setTransform(oldTransform);
                } else {
                    // Draw normally (facing right)
                    g2d.drawImage(hookImage, boat.getHookX() - 5, boat.getHookY(), 10, 20, this);
                }
            } else {
                g.fillRect(boat.getHookX() - 2, boat.getHookY(), 4, 10); // Domyślny hak
            }
        }// Rysowanie standardowych ryb - pixel art style
        if (normalFishImage != null) {
            for (int i = 0; i < fishCount; i++) {
                if (fishY[i] >= 0) { // Tylko jeśli ryba jest widoczna
                    g2d.drawImage(normalFishImage, fishX[i], fishY[i], fishWidth, fishHeight, this);
                }
            }
        } else {
            // Fallback pixel art fish
            for (int i = 0; i < fishCount; i++) {
                if (fishY[i] >= 0) {
                    // Ciało ryby
                    g.setColor(new Color(255, 165, 0));
                    g.fillRect(fishX[i] + 5, fishY[i] + 5, 18, 10);
                    
                    // Ogon pixel art
                    g.fillRect(fishX[i], fishY[i] + 8, 8, 4);
                    
                    // Oko
                    g.setColor(Color.BLACK);
                    g.fillRect(fishX[i] + 20, fishY[i] + 7, 3, 3);
                    g.setColor(Color.WHITE);
                    g.fillRect(fishX[i] + 21, fishY[i] + 8, 1, 1);
                }
            }
        }

        // Rysowanie ryb Fish2 (czerwonych)
        // Fish2 ma własną metodę draw, która używa obrazka
        for (Fish2 fish2 : fish2Array) {
            if (fish2.isVisible()) {
                fish2.draw(g2d); // Używamy metody draw z klasy Fish2
            }
        }        // Wyświetlanie tekstu - pixel art style z cieniem
        g.setFont(new Font("Monospaced", Font.BOLD, 16));
        
        // Cień tekstu
        g.setColor(new Color(0, 0, 0, 100));
        g.drawString("Złapane ryby: " + fishCaught, getWidth() - 219, 31);
        g.drawString("Pieniądze: " + money + " PLN", getWidth() - 219, 51);
        
        // Główny tekst - żółty pixel art style
        g.setColor(new Color(255, 255, 0));
        g.drawString("Złapane ryby: " + fishCaught, getWidth() - 220, 30);
        g.drawString("Pieniądze: " + money + " PLN", getWidth() - 220, 50);
        
        // Obramowanie tekstu dla lepszej czytelności
        g.setColor(Color.BLACK);
        g.drawRect(getWidth() - 225, 10, 215, 45);
    }    @Override
    public void actionPerformed(ActionEvent e) {
        if (gamePaused) return;

        // Aktualizacja paralaksy
        skyOffsetX -= parallaxSpeed * 0.3f; // Niebo porusza się wolniej
        waterOffsetX -= parallaxSpeed * 1.2f; // Woda porusza się szybciej
        
        // Resetuj offset gdy wykracza poza zakres
        if (skyOffsetX <= -100) skyOffsetX = 0;
        if (waterOffsetX <= -100) waterOffsetX = 0;
        
        // Responsive boat movement
        if (leftPressed) {
            boat.moveLeft();
        }
        if (rightPressed) {
            boat.moveRight();
        }

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
    }    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            leftPressed = true;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            rightPressed = true;
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
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            leftPressed = false;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        } else if (e.getKeyCode() == KeyEvent.VK_UP) {
            upArrowPressed = false;
        }
    }
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
    }    public void saveBoatUpgradeLevel() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(boatUpgradeFile))) {
            writer.write(String.valueOf(boat.getBoatUpgradeLevel()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }    // Metody do tworzenia grafiki pixel art w stylu Stardew Valley
    private BufferedImage createPixelArtBoat() {
        BufferedImage image = new BufferedImage(60, 30, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        
        // Kadłub łodzi - ciepły brąz Stardew Valley style
        g.setColor(new Color(160, 82, 45));
        g.fillRect(4, 15, 52, 12);
        
        // Cienie kadłuba dla głębi
        g.setColor(new Color(139, 69, 19));
        g.fillRect(4, 23, 52, 4);
        
        // Dziób łodzi - zaokrąglony
        g.setColor(new Color(160, 82, 45));
        g.fillRect(0, 18, 8, 6);
        g.setColor(new Color(139, 69, 19));
        g.fillRect(0, 21, 6, 3);
        
        // Żagiel - kremowy z detalami
        g.setColor(new Color(255, 248, 220));
        g.fillRect(24, 2, 4, 18);
        g.fillRect(14, 5, 16, 12);
        
        // Cienie żagla
        g.setColor(new Color(240, 230, 200));
        g.fillRect(26, 6, 2, 11);
        
        // Maszt - ciemny brąz
        g.setColor(new Color(101, 67, 33));
        g.fillRect(26, 2, 2, 20);
        
        // Dekoracyjne elementy łodzi
        g.setColor(new Color(255, 215, 0));
        g.fillRect(10, 19, 2, 2);
        g.fillRect(45, 19, 2, 2);
        
        g.dispose();
        return image;
    }    private BufferedImage createPixelArtFish(boolean isRed) {
        BufferedImage image = new BufferedImage(30, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        
        // Kolory Stardew Valley style
        Color fishColor = isRed ? new Color(220, 80, 80) : new Color(255, 140, 60);
        Color shadowColor = isRed ? new Color(180, 60, 60) : new Color(215, 115, 45);
        Color lightColor = isRed ? new Color(255, 120, 120) : new Color(255, 170, 100);
        
        // Ciało ryby - główny kolor
        g.setColor(fishColor);
        g.fillOval(6, 6, 16, 8);
        
        // Cień ryby dla głębi
        g.setColor(shadowColor);
        g.fillOval(7, 9, 14, 5);
        
        // Światło na rybie
        g.setColor(lightColor);
        g.fillOval(8, 7, 12, 3);
        
        // Ogon - bardziej stylowy
        int[] xPoints = {6, 0, 2, 6};
        int[] yPoints = {8, 10, 12, 12};
        g.setColor(fishColor);
        g.fillPolygon(xPoints, yPoints, 4);
        
        // Cień ogona
        g.setColor(shadowColor);
        int[] xShadow = {4, 0, 1, 4};
        int[] yShadow = {10, 11, 12, 12};
        g.fillPolygon(xShadow, yShadow, 4);
        
        // Oko - większe i bardziej ekspresyjne Stardew style
        g.setColor(Color.BLACK);
        g.fillOval(16, 7, 4, 4);
        
        // Światełko w oku - białe
        g.setColor(Color.WHITE);
        g.fillOval(17, 8, 2, 2);
        g.fillOval(18, 7, 1, 1);
        
        // Płetwy - bardziej kolorowe
        g.setColor(fishColor.darker());
        g.fillOval(12, 4, 5, 3); // górna płetwa
        g.fillOval(12, 13, 5, 3); // dolna płetwa
        g.fillOval(14, 11, 3, 2); // boczna płetwa
        
        // Detale - paski na rybie
        g.setColor(shadowColor.darker());
        g.fillRect(9, 8, 1, 4);
        g.fillRect(13, 7, 1, 6);
        g.fillRect(17, 8, 1, 4);
        
        g.dispose();
        return image;
    }    private BufferedImage createPixelArtHook() {
        BufferedImage image = new BufferedImage(12, 15, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        
        // Haczyk w stylu Stardew Valley - metaliczno-srebrny
        g.setColor(new Color(180, 180, 190)); // Jasny metaliczny
        g.fillRect(5, 0, 2, 8); // Główny pręt
        
        // Zagięcie haczyka - bardziej stylizowane
        g.fillRect(7, 8, 3, 2);
        g.fillRect(9, 10, 2, 3);
        g.fillRect(8, 13, 3, 2);
        
        // Cienie dla głębi - charakterystyczne dla ST
        g.setColor(new Color(120, 120, 130)); // Ciemny metaliczny
        g.fillRect(6, 1, 1, 7); // Cień pręta
        g.fillRect(8, 9, 1, 1);
        g.fillRect(10, 11, 1, 2);
        g.fillRect(9, 14, 1, 1);
        
        // Błysk metaliczny - detal ST
        g.setColor(new Color(220, 220, 235)); // Bardzo jasny metaliczny
        g.fillRect(5, 2, 1, 3);
        g.fillRect(7, 8, 1, 1);
        
        // Ostrze haczyka
        g.setColor(new Color(140, 140, 150));
        g.fillRect(10, 13, 1, 1);
        
        g.dispose();
        return image;
    }    private BufferedImage createPixelArtSky() {
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        
        // Gradient nieba - Stardew Valley style z cieplejszymi kolorami
        for (int y = 0; y < 200; y++) {
            float ratio = (float)y / 200;
            int blue = (int)(230 + ratio * 25);
            int green = (int)(220 - ratio * 30);
            int red = (int)(180 + ratio * 40);
            g.setColor(new Color(Math.min(255, red), Math.min(255, green), Math.min(255, blue)));
            g.fillRect(0, y, 200, 1);
        }
        
        // Chmury w stylu Stardew Valley - bardziej miękkie i pusziste
        g.setColor(new Color(255, 255, 255, 220));
        
        // Duża chmura 1 - bardziej organiczny kształt
        g.fillRect(20, 30, 35, 12);
        g.fillRect(18, 32, 39, 8);
        g.fillRect(25, 28, 25, 6);
        g.fillRect(30, 26, 15, 4);
        g.fillRect(35, 24, 8, 3); // Dodatkowa puszystość
        
        // Średnia chmura 2 - z większą ilością detali
        g.fillRect(120, 45, 28, 10);
        g.fillRect(118, 47, 32, 6);
        g.fillRect(125, 43, 18, 4);
        g.fillRect(130, 41, 10, 3);
        
        // Mała chmura 3 - bardziej zaokrąglona
        g.fillRect(70, 20, 20, 8);
        g.fillRect(68, 22, 24, 4);
        g.fillRect(72, 18, 16, 3);
        
        // Dodatkowe chmury w ST stylu
        g.fillRect(160, 70, 25, 8);
        g.fillRect(158, 72, 29, 4);
        g.fillRect(162, 68, 18, 3);
        
        g.fillRect(40, 80, 30, 10);
        g.fillRect(38, 82, 34, 6);
        g.fillRect(43, 78, 24, 3);
        
        // Delikatne cienie chmur - Stardew Valley shadow effect
        g.setColor(new Color(240, 240, 250, 80));
        g.fillRect(22, 38, 31, 4);
        g.fillRect(122, 51, 24, 3);
        g.fillRect(72, 26, 16, 2);
        g.fillRect(162, 76, 21, 2);
        g.fillRect(42, 88, 26, 3);
        
        // Słońce - charakterystyczne dla Stardew Valley
        g.setColor(new Color(255, 255, 150, 180));
        g.fillOval(150, 15, 20, 20);
        g.setColor(new Color(255, 255, 200, 120));
        g.fillOval(148, 13, 24, 24); // Blask słońca
        
        g.dispose();
        return image;
    }    private BufferedImage createPixelArtWater() {
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        
        // Podstawowy kolor wody - Stardew Valley style z cieplejszymi niebieskimi
        for (int y = 0; y < 200; y++) {
            float depth = (float)y / 200;
            int blue = (int)(160 + depth * 35);
            int green = (int)(115 + depth * 25);
            int red = (int)(30 + depth * 10); // Dodajemy trochę czerwieni dla ciepłości
            g.setColor(new Color(Math.min(255, red), Math.min(255, green), Math.min(255, blue)));
            g.fillRect(0, y, 200, 1);
        }
        
        // Fale pixel art - Stardew Valley style bardziej stylizowane
        g.setColor(new Color(85, 170, 235)); // Jasnoniebieski dla fal
        for (int y = 10; y < 200; y += 28) {
            for (int x = 0; x < 200; x += 35) {
                // Główne fale - bardziej organiczny kształt
                g.fillRect(x, y, 14, 4);
                g.fillRect(x + 2, y - 1, 10, 2);
                g.fillRect(x + 4, y - 2, 6, 1);
                
                // Druga fala - mniejsza
                g.fillRect(x + 20, y + 6, 12, 3);
                g.fillRect(x + 22, y + 5, 8, 2);
                g.fillRect(x + 24, y + 4, 4, 1);
            }
        }
        
        // Ciemniejsze fale - głębsze warstwy z większymi detalami
        g.setColor(new Color(50, 120, 180));
        for (int y = 22; y < 200; y += 32) {
            for (int x = 12; x < 200; x += 40) {
                g.fillRect(x, y, 10, 3);
                g.fillRect(x + 2, y - 1, 6, 2);
                g.fillRect(x + 25, y + 4, 8, 2);
                g.fillRect(x + 27, y + 3, 4, 1);
            }
        }
        
        // Refleksy światła na wodzie - bardziej Stardew Valley style
        g.setColor(new Color(150, 220, 255, 140));
        for (int y = 8; y < 200; y += 45) {
            for (int x = 8; x < 200; x += 55) {
                g.fillRect(x, y, 4, 2);
                g.fillRect(x + 1, y - 1, 2, 1);
                g.fillRect(x + 30, y + 12, 3, 1);
                g.fillRect(x + 31, y + 11, 1, 1);
            }
        }
        
        // Dodatkowe detale - bąbelki i iskierki jak w Stardew Valley
        g.setColor(new Color(200, 240, 255, 100));
        Random rand = new Random(42); // Seed dla konsystentności
        for (int i = 0; i < 15; i++) {
            int x = rand.nextInt(180);
            int y = rand.nextInt(180) + 20;
            g.fillRect(x, y, 2, 2);
            if (rand.nextBoolean()) {
                g.fillRect(x - 1, y - 1, 1, 1);
            }
        }
        
        g.dispose();
        return image;
    }    private BufferedImage createPixelArtShop() {
        BufferedImage image = new BufferedImage(150, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        
        // Podstawa sklepu - ciepły brąz w stylu Stardew Valley
        g.setColor(new Color(120, 80, 50));
        g.fillRect(0, 20, 150, 80);
        
        // Cienie ścian dla głębi
        g.setColor(new Color(90, 60, 35));
        g.fillRect(0, 85, 150, 15);
        g.fillRect(140, 20, 10, 80);
        
        // Dach - ciepły czerwony z detalami
        g.setColor(new Color(180, 60, 50));
        g.fillRect(0, 0, 150, 25);
        
        // Cienie dachu
        g.setColor(new Color(150, 45, 35));
        g.fillRect(0, 18, 150, 7);
        
        // Okno - bardziej szczegółowe w ST stylu
        g.setColor(new Color(160, 200, 240)); // Jasnoniebieski
        g.fillRect(20, 35, 25, 20);
        
        // Rama okna
        g.setColor(new Color(80, 50, 30));
        g.drawRect(19, 34, 26, 21);
        g.fillRect(31, 35, 2, 20); // pionowy słupek
        g.fillRect(20, 44, 25, 2); // poziomy słupek
        
        // Odbicie w oknie
        g.setColor(new Color(220, 240, 255, 120));
        g.fillRect(22, 37, 8, 6);
        
        // Drzwi - bardziej stylowe
        g.setColor(new Color(140, 90, 55));
        g.fillRect(70, 40, 30, 60);
        
        // Detale drzwi
        g.setColor(new Color(160, 110, 70));
        g.fillRect(72, 42, 26, 5); // górna listwa
        g.fillRect(72, 92, 26, 5); // dolna listwa
        g.fillRect(72, 65, 26, 3); // środkowa listwa
        
        // Klamka - złota
        g.setColor(new Color(220, 180, 50));
        g.fillOval(92, 65, 4, 4);
        g.setColor(new Color(200, 160, 40)); // cień klamki
        g.fillOval(93, 66, 2, 2);
        
        // Szyld - bardziej Stardew Valley style
        g.setColor(new Color(245, 235, 215)); // Kremowy
        g.fillRect(110, 30, 35, 15);
        
        // Rama szyldu
        g.setColor(new Color(120, 80, 50));
        g.drawRect(109, 29, 36, 16);
        
        // Tekst szyldu
        g.setColor(new Color(80, 50, 30));
        g.setFont(new Font("Monospaced", Font.BOLD, 10));
        g.drawString("SKLEP", 115, 41);
        
        // Dodatkowe detale - beczki obok sklepu
        g.setColor(new Color(101, 67, 33));
        g.fillOval(5, 75, 12, 20);
        g.fillOval(130, 80, 10, 15);
        
        // Obręcze beczek
        g.setColor(new Color(70, 50, 30));
        g.fillRect(5, 82, 12, 2);
        g.fillRect(130, 85, 10, 2);
        
        // Trawa przy sklepie
        g.setColor(new Color(80, 120, 50));
        for (int x = 0; x < 150; x += 8) {
            g.fillRect(x, 95, 3, 5);
            g.fillRect(x + 4, 97, 2, 3);
        }
        
        g.dispose();
        return image;
    }
}
