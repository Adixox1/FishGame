

/**
 * Reprezentuje łódź gracza, jej ruch, fizykę oraz mechanikę haka.
 * Klasa zarządza pozycją łodzi, jej prędkością z uwzględnieniem pędu,
 * a także opuszczaniem i zwijaniem haka.
 */
public class Boat {
    private int boatX;
    private int boatY;
    private final int boatWidth = 80;
    private final int boatHeight = 30;
    private int hookX, hookY;
    private boolean hookDropped = false;
    private final int hookSpeed = 5;
    private int screenWidth;
    private double speedMultiplier = 1.0;
    private final int baseSpeed = 8;
    private int hookUpgradeLevel = 0;
    private int boatUpgradeLevel = 0;

    // Pola odpowiedzialne za symulację fizyki łodzi (płynny ruch)
    private float velocityX = 0f;
    private final float acceleration = 1.2f;
    private final float friction = 0.85f; // Współczynnik tarcia, spowalnia łódź
    private final float maxSpeed = 12f;

    // Zmienna do śledzenia kierunku łodzi (dla odbicia lustrzanego grafiki)
    private boolean facingLeft = false;

    /**
     * Konstruktor klasy Boat.
     * @param screenWidth Szerokość ekranu gry.
     * @param waterLevel Poziom wody, na którym unosi się łódź.
     */
    public Boat(int screenWidth, int waterLevel) {
        this.screenWidth = screenWidth;
        this.boatX = screenWidth / 2 - boatWidth / 2; // Początkowa pozycja na środku ekranu
        this.boatY = waterLevel - boatHeight;
        this.hookX = boatX + boatWidth / 2;
        this.hookY = boatY + boatHeight;
    }

    /**
     * Przyspiesza łódź w lewo.
     */
    public void moveLeft() {
        velocityX -= acceleration * (speedMultiplier + boatUpgradeLevel * 0.2f);
        velocityX = Math.max(velocityX, -maxSpeed); // Ograniczenie maksymalnej prędkości
        facingLeft = true;
        updatePosition();
    }

    /**
     * Przyspiesza łódź w prawo.
     */
    public void moveRight() {
        velocityX += acceleration * (speedMultiplier + boatUpgradeLevel * 0.2f);
        velocityX = Math.min(velocityX, maxSpeed); // Ograniczenie maksymalnej prędkości
        facingLeft = false;
        updatePosition();
    }

    /**
     * Aktualizuje pozycję łodzi na podstawie jej prędkości i tarcia.
     * Zapobiega wyjściu łodzi poza ekran.
     */
    private void updatePosition() {
        boatX += (int)velocityX;
        boatX = Math.max(0, Math.min(screenWidth - boatWidth, boatX)); // Blokada na krawędziach ekranu

        // Haczyk podąża za łódką, jego pozycja zależy od kierunku zwrotu łodzi
        if (facingLeft) {
            hookX = boatX + (int)(boatWidth * 0.25);
        } else {
            hookX = boatX + (int)(boatWidth * 0.75);
        }

        // Zastosowanie tarcia do stopniowego wyhamowania łodzi
        velocityX *= friction;
        if (Math.abs(velocityX) < 0.1f) {
            velocityX = 0f;
        }
    }

    /**
     * Rozpoczyna opuszczanie haka, jeśli nie jest już opuszczony.
     */
    public void dropHook() {
        if (!hookDropped) {
            hookDropped = true;
            // Pozycja haka jest dostosowana do kierunku łodzi
            if (facingLeft) {
                hookX = boatX + (int)(boatWidth * 0.25);
            } else {
                hookX = boatX + (int)(boatWidth * 0.75);
            }
        }
    }

    /**
     * Aktualizuje pozycję haka oraz łodzi (jej tarcie).
     * @param panelHeight Wysokość panelu gry, używana do określenia maksymalnej głębokości haka.
     */
    public void updateHook(int panelHeight) {
        updatePosition(); // Aktualizacja pozycji łodzi (efekt tarcia)

        if (hookDropped) {
            // Prędkość haka jest zwiększana przez ulepszenia
            hookY += hookSpeed + (hookUpgradeLevel * 1);
            // Haczyk jest zwijany po osiągnięciu dna (z marginesem)
            if (hookY > panelHeight - 20) {
                hookDropped = false;
                hookY = boatY + boatHeight;
            }
        }
    }

    /**
     * Resetuje (zwija) hak do pozycji początkowej przy łodzi.
     */
    public void resetHook() {
        hookDropped = false;
        // Pozycja haka jest dostosowana do kierunku łodzi
        if (facingLeft) {
            hookX = boatX + (int)(boatWidth * 0.25);
        } else {
            hookX = boatX + (int)(boatWidth * 0.75);
        }
        hookY = boatY + boatHeight;
    }

    // Gettery i Settery
    public int getBoatX() { return boatX; }
    public int getBoatY() { return boatY; }
    public int getBoatWidth() { return boatWidth; }
    public int getBoatHeight() { return boatHeight; }
    public int getHookX() { return hookX; }
    public int getHookY() { return hookY; }
    public boolean isHookDropped() { return hookDropped; }
    public void setBoatX(int x) { this.boatX = x; }
    public void setSpeedMultiplier(double multiplier) { this.speedMultiplier = multiplier; }
    public void setHookUpgradeLevel(int level) { this.hookUpgradeLevel = level; }
    public int getHookUpgradeLevel() { return hookUpgradeLevel; }
    public void setBoatUpgradeLevel(int level) { this.boatUpgradeLevel = level; }
    public int getBoatUpgradeLevel() { return boatUpgradeLevel; }
    public boolean isFacingLeft() { return facingLeft; }
}