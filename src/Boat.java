public class Boat {
    private int boatX;
    private int boatY;
    private final int boatWidth = 80;
    private final int boatHeight = 30;
    private int hookX, hookY;
    private boolean hookDropped = false;
    private final int hookSpeed = 5;
    private int screenWidth;
    private double speedMultiplier = 1.0;  // Dodajemy mnożnik prędkości
    private final int baseSpeed = 5;   // Bazowa prędkość łodzi
    private int hookUpgradeLevel = 0;  // Dodajemy poziom ulepszenia haczyka
    private int boatUpgradeLevel = 0;  // Dodajemy poziom ulepszenia łodzi

    public Boat(int screenWidth, int waterLevel) {
        this.screenWidth = screenWidth;
        // Ustawiamy łódź na środku ekranu, tuż nad poziomem wody
        boatX = screenWidth / 2 - boatWidth / 2;
        boatY = waterLevel - boatHeight;
        hookX = boatX + boatWidth / 2;
        hookY = boatY + boatHeight;
    }

    public void moveLeft() {
        boatX = Math.max(0, boatX - (int)(baseSpeed * (speedMultiplier + boatUpgradeLevel * 0.2)));  // Używamy mnożnika i ulepszenia łodzi
        // Aktualizowanie pozycji haczyka, gdy łódź porusza się w lewo
        if (!hookDropped) {
            hookX = boatX + boatWidth / 2;
        }
    }

    public void moveRight() {
        boatX = Math.min(screenWidth - boatWidth, boatX + (int)(baseSpeed * (speedMultiplier + boatUpgradeLevel * 0.2))); // Używamy mnożnika i ulepszenia łodzi
        // Aktualizowanie pozycji haczyka, gdy łódź porusza się w prawo
        if (!hookDropped) {
            hookX = boatX + boatWidth / 2;
        }
    }

    public void dropHook() {
        if (!hookDropped) {
            hookDropped = true;
            hookX = boatX + boatWidth / 2;  // Ustawienie pozycji haczyka nad łódką
        }
    }

    public void updateHook(int panelHeight) {
        if (hookDropped) {
            // Prędkość haka może być zwiększona przez ulepszenie
            hookY += hookSpeed + (hookUpgradeLevel * 1); // Zwiększamy prędkość haka o 1 piksel na poziom ulepszenia
            // Haczyk zatrzyma się 20 pikseli przed dolną krawędzią ekranu
            if (hookY > panelHeight - 20) {
                hookDropped = false;
                hookY = boatY + boatHeight;
            }
        }

        // Haczyk podąża za łódką, nawet jeśli jest opuszczony
        hookX = boatX + boatWidth / 2;
    }

    public void resetHook() {
        hookDropped = false;
        hookX = boatX + boatWidth / 2;
        hookY = boatY + boatHeight;
    }

    // Gettery dla rysowania
    public int getBoatX() { return boatX; }
    public int getBoatY() { return boatY; }
    public int getBoatWidth() { return boatWidth; }
    public int getBoatHeight() { return boatHeight; }
    public int getHookX() { return hookX; }
    public int getHookY() { return hookY; }
    public boolean isHookDropped() { return hookDropped; }

    // Setter dla pozycji X łodzi
    public void setBoatX(int x) { this.boatX = x; }

    public void setSpeedMultiplier(double multiplier) {  // Setter dla mnożnika
        this.speedMultiplier = multiplier;
    }

    public void setHookUpgradeLevel(int level) {  // Dodajemy setter dla poziomu ulepszenia haczyka
        this.hookUpgradeLevel = level;
    }

    public int getHookUpgradeLevel() {  // Dodajemy getter dla poziomu ulepszenia haczyka
        return hookUpgradeLevel;
    }

    public void setBoatUpgradeLevel(int level) {  // Dodajemy setter dla poziomu ulepszenia łodzi
        this.boatUpgradeLevel = level;
    }

    public int getBoatUpgradeLevel() {  // Dodajemy getter dla poziomu ulepszenia łodzi
        return boatUpgradeLevel;
    }
}
