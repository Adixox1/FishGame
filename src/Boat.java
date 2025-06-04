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
    private final int baseSpeed = 8;   // Zwiększona bazowa prędkość dla responsywności
    private int hookUpgradeLevel = 0;
    private int boatUpgradeLevel = 0;
      // Dodajemy momentum dla płynniejszego sterowania
    private float velocityX = 0f;
    private final float acceleration = 1.2f;
    private final float friction = 0.85f;
    private final float maxSpeed = 12f;
    
    // Direction tracking for boat mirroring
    private boolean facingLeft = false;

    public Boat(int screenWidth, int waterLevel) {
        this.screenWidth = screenWidth;
        // Ustawiamy łódź na środku ekranu, tuż nad poziomem wody
        boatX = screenWidth / 2 - boatWidth / 2;
        boatY = waterLevel - boatHeight;
        hookX = boatX + boatWidth / 2;
        hookY = boatY + boatHeight;
    }    public void moveLeft() {
        velocityX -= acceleration * (speedMultiplier + boatUpgradeLevel * 0.2f);
        velocityX = Math.max(velocityX, -maxSpeed);
        facingLeft = true; // Update direction
        updatePosition();
    }

    public void moveRight() {
        velocityX += acceleration * (speedMultiplier + boatUpgradeLevel * 0.2f);
        velocityX = Math.min(velocityX, maxSpeed);
        facingLeft = false; // Update direction
        updatePosition();
    }
      private void updatePosition() {
        boatX += (int)velocityX;
        boatX = Math.max(0, Math.min(screenWidth - boatWidth, boatX));
        
        // Aktualizowanie pozycji haczyka gdy łódź się porusza (tylko gdy haczyk nie jest opuszczony)
        if (!hookDropped) {
            if (facingLeft) {
                hookX = boatX + (int)(boatWidth * 0.25);  // Left side when facing left
            } else {
                hookX = boatX + (int)(boatWidth * 0.75);  // Right side when facing right
            }
        }
        
        // Zastosuj tarcie
        velocityX *= friction;
        if (Math.abs(velocityX) < 0.1f) {
            velocityX = 0f;
        }
    }public void dropHook() {
        if (!hookDropped) {
            hookDropped = true;
            // Position hook based on boat direction
            if (facingLeft) {
                hookX = boatX + (int)(boatWidth * 0.25);  // Left side when facing left
            } else {
                hookX = boatX + (int)(boatWidth * 0.75);  // Right side when facing right
            }
        }
    }    public void updateHook(int panelHeight) {
        // Aktualizuj pozycję łodzi (tarcie)
        updatePosition();
        
        if (hookDropped) {
            // Prędkość haka może być zwiększona przez ulepszenie
            hookY += hookSpeed + (hookUpgradeLevel * 1); // Zwiększamy prędkość haka o 1 piksel na poziom ulepszenia
            // Haczyk zatrzyma się 20 pikseli przed dolną krawędzią ekranu
            if (hookY > panelHeight - 20) {
                hookDropped = false;
                hookY = boatY + boatHeight;
            }
        }

        // Update hook X position based on boat direction and movement
        if (!hookDropped) {
            // When hook is not dropped, position it based on boat direction
            if (facingLeft) {
                hookX = boatX + (int)(boatWidth * 0.25);  // Left side when facing left
            } else {
                hookX = boatX + (int)(boatWidth * 0.75);  // Right side when facing right
            }
        }
        // If hook is dropped, maintain its X position (it doesn't follow boat horizontally)
    }    public void resetHook() {
        hookDropped = false;
        // Position hook based on boat direction
        if (facingLeft) {
            hookX = boatX + (int)(boatWidth * 0.25);  // Left side when facing left
        } else {
            hookX = boatX + (int)(boatWidth * 0.75);  // Right side when facing right
        }
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
    }    public int getBoatUpgradeLevel() {  // Dodajemy getter dla poziomu ulepszenia łodzi
        return boatUpgradeLevel;
    }
    
    public boolean isFacingLeft() {  // Getter for boat direction
        return facingLeft;
    }
}
