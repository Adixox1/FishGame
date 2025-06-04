import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Fish2 {
    private int x, y;
    private int speed;
    private final int width = 30;
    private final int height = 20;
    private int screenWidth;
    private Random random = new Random();
    private int waterLevel;
    private int screenHeight;
    private BufferedImage redFishImage;

    public Fish2(int screenWidth, int waterLevel, int screenHeight) {
        this.screenWidth = screenWidth;
        this.waterLevel = waterLevel;
        this.screenHeight = screenHeight;
        this.x = random.nextInt(screenWidth - width);
        int minFishY = waterLevel + 50;
        this.y = random.nextInt(screenHeight - minFishY - height) + minFishY;
        this.speed = random.nextBoolean() ? 5 : -5;

        // Tworzenie grafiki pixel art dla czerwonej ryby
        try {
            redFishImage = createPixelArtRedFish();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Błąd tworzenia grafiki pixel art dla Fish2.");
        }
    }

    public void move() {
        x += speed;
        // Zmieniamy warunki odbijania ryb
        if (x <= 0) {
            x = 0; // Upewniamy się, że ryba nie wyjdzie poza lewą krawędź
            speed = -speed;
        } else if (x >= screenWidth - width) {
            x = screenWidth - width; // Upewniamy się, że ryba nie wyjdzie poza prawą krawędź
            speed = -speed;
        }
        if (random.nextInt(100) < 5) {
            speed = random.nextBoolean() ? 5 : -5;
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        
        // Rysowanie obrazka czerwonej ryby, jeśli został wczytany
        if (redFishImage != null) {
            g2d.drawImage(redFishImage, x, y, width, height, null);
        } else {
            // Pixel art czerwona ryba fallback
            g.setColor(new Color(220, 20, 20));
            g.fillRect(x + 5, y + 5, 18, 10);
            
            // Ogon pixel art
            g.fillRect(x, y + 8, 8, 4);
            
            // Oko
            g.setColor(Color.BLACK);
            g.fillRect(x + 20, y + 7, 3, 3);
            g.setColor(Color.WHITE);
            g.fillRect(x + 21, y + 8, 1, 1);
            
            // Płetwy
            g.setColor(new Color(180, 15, 15));
            g.fillRect(x + 12, y + 3, 4, 3); // górna płetwa
            g.fillRect(x + 12, y + 14, 4, 3); // dolna płetwa
        }
    }    private BufferedImage createPixelArtRedFish() {
        BufferedImage image = new BufferedImage(30, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        
        // Stardew Valley style - ciało ryby z ciepłymi kolorami
        g.setColor(new Color(205, 85, 65)); // Ciepły czerwono-pomarańczowy
        g.fillOval(5, 5, 18, 10);
        
        // Brzuch - jaśniejszy i cieplejszy
        g.setColor(new Color(235, 150, 120)); // Jasny łososiowy
        g.fillOval(7, 8, 14, 4);
        
        // Cienie i kontur - charakterystyczne dla Stardew Valley
        g.setColor(new Color(145, 55, 35)); // Ciemny brąz
        g.drawOval(5, 5, 18, 10); // Kontur ciała
        
        // Ogon - bardziej stylizowany w ST style
        int[] xPoints = {5, 0, 2, 5, 3};
        int[] yPoints = {8, 9, 11, 12, 10};
        g.setColor(new Color(185, 70, 50));
        g.fillPolygon(xPoints, yPoints, 5);
        
        // Kontur ogona
        g.setColor(new Color(145, 55, 35));
        g.drawPolygon(xPoints, yPoints, 5);
        
        // Oko - duże i charakterystyczne dla Stardew Valley
        g.setColor(Color.BLACK);
        g.fillOval(16, 6, 6, 6);
        
        // Tęczówka
        g.setColor(new Color(70, 130, 180)); // Niebieski
        g.fillOval(17, 7, 4, 4);
        
        // Światełko w oku - podwójne dla ST efektu
        g.setColor(Color.WHITE);
        g.fillOval(18, 7, 2, 2);
        g.fillRect(17, 8, 1, 1);
        
        // Płetwy - więcej detali w ST stylu
        g.setColor(new Color(185, 70, 50));
        g.fillOval(11, 1, 7, 5); // górna płetwa
        g.fillOval(11, 14, 7, 5); // dolna płetwa
        g.fillOval(13, 11, 5, 3); // boczna płetwa
        
        // Kontury płetw
        g.setColor(new Color(145, 55, 35));
        g.drawOval(11, 1, 7, 5);
        g.drawOval(11, 14, 7, 5);
        g.drawOval(13, 11, 5, 3);
        
        // Łuski - wzór charakterystyczny dla Stardew Valley
        g.setColor(new Color(225, 120, 90)); // Jasny akcent
        g.fillOval(8, 6, 3, 2);
        g.fillOval(12, 7, 3, 2);
        g.fillOval(15, 8, 3, 2);
        g.fillOval(10, 9, 3, 2);
        g.fillOval(13, 10, 3, 2);
        
        // Dodatkowe detale - paski jak w ST
        g.setColor(new Color(165, 60, 45));
        g.fillRect(9, 7, 1, 1);
        g.fillRect(11, 8, 1, 1);
        g.fillRect(14, 9, 1, 1);
        g.fillRect(16, 10, 1, 1);
        
        // Usta - mały detal
        g.setColor(new Color(145, 55, 35));
        g.fillOval(23, 9, 2, 2);
        
        g.dispose();
        return image;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void remove() {
        y = -height;
    }

    public boolean isVisible() {
        return y >= 0;
    }

    // Dodane gettery dla szerokości i wysokości, aby BoatGame mogło ich użyć do rysowania
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
