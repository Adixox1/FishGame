import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Reprezentuje specjalny, rzadszy i cenniejszy typ ryby w grze.
 * Obiekt tej klasy porusza się szybciej niż standardowe ryby i posiada
 * unikalną, proceduralnie generowaną grafikę w stylu pixel art.
 */
public class Fish2 {
    private int x, y;
    private int speed;
    private final int width = 50;
    private final int height = 40;
    private int screenWidth;
    private Random random = new Random();
    private int waterLevel;
    private int screenHeight;
    private BufferedImage redFishImage;
    private boolean facingLeft = false; // Śledzi kierunek, w którym zwrócona jest ryba

    /**
     * Konstruktor tworzący instancję specjalnej ryby.
     * Inicjalizuje jej pozycję, prędkość oraz generuje jej unikalną grafikę.
     *
     * @param screenWidth Szerokość ekranu gry, używana do ograniczenia ruchu.
     * @param waterLevel Poziom wody, poniżej którego ryba się pojawia.
     * @param screenHeight Wysokość ekranu gry, używana do określenia zakresu pojawiania się ryby.
     */
    public Fish2(int screenWidth, int waterLevel, int screenHeight) {
        this.screenWidth = screenWidth;
        this.waterLevel = waterLevel;
        this.screenHeight = screenHeight;
        this.x = random.nextInt(screenWidth - width);
        int minFishY = waterLevel + 50;
        this.y = random.nextInt(screenHeight - minFishY - height) + minFishY;
        this.speed = random.nextBoolean() ? 5 : -5; // Ten typ ryby jest szybszy
        this.facingLeft = this.speed < 0; // Ustawienie początkowego kierunku

        try {
            redFishImage = createPixelArtRedFish();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Błąd tworzenia grafiki pixel art dla Fish2.");
        }
    }

    /**
     * Aktualizuje pozycję ryby w każdej klatce.
     * Implementuje ruch poziomy, odbijanie się od krawędzi ekranu
     * oraz losową zmianę kierunku.
     */
    public void move() {
        x += speed;
        // Logika odbijania się ryby od bocznych krawędzi ekranu
        if (x <= 0) {
            x = 0;
            speed = -speed;
        } else if (x >= screenWidth - width) {
            x = screenWidth - width;
            speed = -speed;
        }
        // Niewielka szansa na losową zmianę kierunku, co urozmaica ruch
        if (random.nextInt(100) < 5) {
            speed = random.nextBoolean() ? 5 : -5;
        }
        // Aktualizacja kierunku na podstawie prędkości
        this.facingLeft = speed < 0;
    }

    /**
     * Rysuje rybę na podanym kontekście graficznym.
     * Wykorzystuje wcześniej wygenerowany obraz `redFishImage`. Jeśli tworzenie obrazu
     * nie powiodło się, rysuje prostą grafikę zastępczą.
     *
     * @param g Kontekst graficzny (Graphics), na którym ryba ma być narysowana.
     */
    public void draw(Graphics g) {
        // Tworzymy kopię obiektu Graphics, aby transformacje nie wpływały na inne elementy
        Graphics2D g2d = (Graphics2D) g.create();
        try {
            // Ustawienia renderingu zapewniające ostry, pixelowy wygląd
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            // Jeśli ryba płynie w lewo, odwracamy grafikę w poziomie
            if (facingLeft) {
                g2d.translate(x + width, y);
                g2d.scale(-1, 1);
                g2d.translate(-x, -y);
            }

            if (redFishImage != null) {
                g2d.drawImage(redFishImage, x, y, width, height, null);
            } else {
                // Zastępcza grafika na wypadek błędu tworzenia obrazu
                // Używamy g2d, aby transformacja została zastosowana
                g2d.setColor(new Color(220, 20, 20));
                g2d.fillRect(x + 5, y + 5, 18, 10);
                g2d.fillRect(x, y + 8, 8, 4);
                g2d.setColor(Color.BLACK);
                g2d.fillRect(x + 20, y + 7, 3, 3);
                g2d.setColor(Color.WHITE);
                g2d.fillRect(x + 21, y + 8, 1, 1);
                g2d.setColor(new Color(180, 15, 15));
                g2d.fillRect(x + 12, y + 3, 4, 3);
                g2d.fillRect(x + 12, y + 14, 4, 3);
            }
        } finally {
            // Zwalniamy zasoby kopii obiektu Graphics
            g2d.dispose();
        }
    }

    /**
     * Proceduralnie generuje obraz BufferedImage przedstawiający rybę.
     * Metoda ta krok po kroku rysuje poszczególne elementy ryby,
     * takie jak ciało, płetwy, oko i łuski, używając określonej palety barw.
     *
     * @return Obiekt BufferedImage zawierający gotową grafikę ryby.
     */
    private BufferedImage createPixelArtRedFish() {
        BufferedImage image = new BufferedImage(30, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        g.setColor(new Color(205, 85, 65));
        g.fillOval(5, 5, 18, 10);

        g.setColor(new Color(235, 150, 120));
        g.fillOval(7, 8, 14, 4);

        g.setColor(new Color(145, 55, 35));
        g.drawOval(5, 5, 18, 10);

        int[] xPoints = {5, 0, 2, 5, 3};
        int[] yPoints = {8, 9, 11, 12, 10};
        g.setColor(new Color(185, 70, 50));
        g.fillPolygon(xPoints, yPoints, 5);

        g.setColor(new Color(145, 55, 35));
        g.drawPolygon(xPoints, yPoints, 5);

        g.setColor(Color.BLACK);
        g.fillOval(16, 6, 6, 6);

        g.setColor(new Color(70, 130, 180));
        g.fillOval(17, 7, 4, 4);

        g.setColor(Color.WHITE);
        g.fillOval(18, 7, 2, 2);
        g.fillRect(17, 8, 1, 1);

        g.setColor(new Color(185, 70, 50));
        g.fillOval(11, 1, 7, 5);
        g.fillOval(11, 14, 7, 5);
        g.fillOval(13, 11, 5, 3);

        g.setColor(new Color(145, 55, 35));
        g.drawOval(11, 1, 7, 5);
        g.drawOval(11, 14, 7, 5);
        g.drawOval(13, 11, 5, 3);

        g.setColor(new Color(225, 120, 90));
        g.fillOval(8, 6, 3, 2);
        g.fillOval(12, 7, 3, 2);
        g.fillOval(15, 8, 3, 2);
        g.fillOval(10, 9, 3, 2);
        g.fillOval(13, 10, 3, 2);

        g.setColor(new Color(165, 60, 45));
        g.fillRect(9, 7, 1, 1);
        g.fillRect(11, 8, 1, 1);
        g.fillRect(14, 9, 1, 1);
        g.fillRect(16, 10, 1, 1);

        g.setColor(new Color(145, 55, 35));
        g.fillOval(23, 9, 2, 2);

        g.dispose();
        return image;
    }

    /**
     * Zwraca prostokątny obszar (hitbox) zajmowany przez rybę.
     * Używane do detekcji kolizji, np. z haczykiem.
     * @return Obiekt Rectangle definiujący granice ryby.
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    /**
     * "Usuwa" rybę z pola gry poprzez ustawienie jej pozycji pionowej
     * poza widocznym obszarem ekranu.
     */
    public void remove() {
        y = -height;
    }

    /**
     * Sprawdza, czy ryba jest aktualnie widoczna na ekranie.
     * @return true, jeśli pozycja Y ryby jest nieujemna; w przeciwnym razie false.
     */
    public boolean isVisible() {
        return y >= 0;
    }

    // Gettery do odczytu prywatnych pól klasy
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}