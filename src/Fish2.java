import java.awt.*;
import java.awt.image.BufferedImage; // Import dla obrazów
import java.io.File; // Import dla obsługi plików
import java.io.IOException; // Import dla obsługi wyjątków I/O
import java.util.Random;
import javax.imageio.ImageIO; // Import dla wczytywania obrazów

public class Fish2 {
    private int x, y;
    private int speed;
    private final int width = 30;
    private final int height = 20;
    private int screenWidth;
    private Random random = new Random();
    private int waterLevel;
    private int screenHeight;
    private BufferedImage redFishImage; // Obrazek dla czerwonej ryby

    public Fish2(int screenWidth, int waterLevel, int screenHeight) {
        this.screenWidth = screenWidth;
        this.waterLevel = waterLevel;
        this.screenHeight = screenHeight;
        this.x = random.nextInt(screenWidth - width);
        int minFishY = waterLevel + 50;
        this.y = random.nextInt(screenHeight - minFishY - height) + minFishY;
        this.speed = random.nextBoolean() ? 5 : -5;

        // Wczytywanie obrazka czerwonej ryby pixel art
        try {
            redFishImage = ImageIO.read(new File("fish_red_pixel.png")); // Upewnij się, że plik istnieje
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Błąd wczytywania obrazka 'fish_red_pixel.png' dla Fish2.");
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
        // Rysowanie obrazka czerwonej ryby, jeśli został wczytany
        if (redFishImage != null) {
            g.drawImage(redFishImage, x, y, width, height, null);
        } else {
            // Domyślne rysowanie prostokąta, jeśli obrazek nie został wczytany
            g.setColor(Color.RED);
            g.fillRect(x, y, width, height);
        }
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
