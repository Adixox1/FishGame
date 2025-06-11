import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * Reprezentuje okno sklepu w grze.
 * Gracz może w nim sprzedawać złowione ryby oraz kupować ulepszenia dla łodzi i haka.
 * Klasa dziedziczy po JFrame, zarządza własnym interfejsem użytkownika,
 * a także odpowiada za wczytywanie i zapisywanie stanu ulepszeń oraz pieniędzy gracza do plików.
 */
public class Shop extends JFrame {

    private int fishCaught;
    private int money;
    private JLabel fishLabel;
    private JLabel moneyLabel;
    private final File fishFile = new File("fish_caught.txt");
    private final File moneyFile = new File("money.txt");
    private BoatGame boatGame;

    // Zmienne przechowujące stan ulepszenia haka
    private int hookUpgradeLevel = 0;
    private final int maxHookUpgradeLevel = 5;
    private int hookUpgradeCost = 200;
    private Rectangle hookUpgradeButtonRect;
    private final File hookUpgradeCostFile = new File("hook_upgrade_cost.txt");

    // Zmienne przechowujące stan ulepszenia łodzi
    private int boatUpgradeLevel = 0;
    private final int maxBoatUpgradeLevel = 5;
    private int boatUpgradeCost = 200;
    private Rectangle boatUpgradeButtonRect;
    private final File boatUpgradeCostFile = new File("boat_upgrade_cost.txt");

    /**
     * Konstruktor okna sklepu.
     * Inicjalizuje komponenty interfejsu użytkownika, wczytuje dane o stanie gry
     * (pieniądze, ulepszenia) i ustawia listenery zdarzeń.
     * @param boatGame Referencja do głównego obiektu gry, potrzebna do komunikacji (np. aktualizacji stanu łodzi).
     */
    public Shop(BoatGame boatGame) {
        this.boatGame = boatGame;
        setTitle("Sklep");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());

        // Wczytanie wszystkich danych przy otwarciu sklepu
        loadFishCaught();
        loadMoney();
        loadHookUpgradeLevel();
        loadBoatUpgradeLevel();
        loadHookUpgradeCost();
        loadBoatUpgradeCost();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Ustawienie czcionki stylizowanej na pixel art dla spójności wizualnej
        Font pixelFont = new Font("Monospaced", Font.BOLD, 16);

        fishLabel = new JLabel("Złowione ryby: " + fishCaught, SwingConstants.CENTER);
        fishLabel.setFont(pixelFont);
        add(fishLabel, gbc);

        gbc.gridy = 1;
        moneyLabel = new JLabel("Pieniądze: " + money + " PLN", SwingConstants.CENTER);
        moneyLabel.setFont(pixelFont);
        add(moneyLabel, gbc);

        gbc.gridy = 2;
        JButton sellButton = new JButton("Sprzedaj rybę (100 PLN)");
        sellButton.setFont(pixelFont);
        sellButton.addActionListener(e -> sellFish());
        // Ustawienia stylu przycisku, aby pasował do estetyki pixel art
        sellButton.setBackground(new Color(100, 150, 255));
        sellButton.setForeground(Color.WHITE);
        sellButton.setFocusPainted(false);
        sellButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        add(sellButton, gbc);

        // Panel do niestandardowego rysowania interfejsu ulepszeń
        JPanel upgradesPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                // Ustawienia renderingu zapewniające ostry, "pixelowy" wygląd
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                drawHookUpgrade(g2d);
                drawBoatUpgrade(g2d);
            }
        };
        gbc.gridy = 3;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(upgradesPanel, gbc);

        // Listener obsługujący kliknięcia na przyciskach ulepszeń narysowanych na panelu
        upgradesPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (hookUpgradeButtonRect != null && hookUpgradeButtonRect.contains(e.getPoint())) {
                    upgradeHook();
                }
                if (boatUpgradeButtonRect != null && boatUpgradeButtonRect.contains(e.getPoint())) {
                    upgradeBoat();
                }
            }
        });

        // Przycisk resetowania ulepszeń
        JButton resetUpgradesButton = new JButton("Resetuj Ulepszenia");
        resetUpgradesButton.setFont(pixelFont);
        resetUpgradesButton.addActionListener(e -> resetUpgrades());
        resetUpgradesButton.setBackground(new Color(255, 100, 100));
        resetUpgradesButton.setForeground(Color.WHITE);
        resetUpgradesButton.setFocusPainted(false);
        resetUpgradesButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        gbc.gridy = 4;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(resetUpgradesButton, gbc);

        // Zapewnia, że łódź gracza odpłynie od sklepu po jego zamknięciu
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                boatGame.moveBoatAwayFromShop();
            }
        });
    }

    /**
     * Obsługuje logikę zakupu ulepszenia haka. Sprawdza, czy gracz ma wystarczająco pieniędzy
     * i czy nie osiągnięto maksymalnego poziomu ulepszenia.
     */
    private void upgradeHook() {
        if (money >= hookUpgradeCost && hookUpgradeLevel < maxHookUpgradeLevel) {
            money -= hookUpgradeCost;
            hookUpgradeLevel++;
            boatGame.getBoat().setHookUpgradeLevel(hookUpgradeLevel); // Aktualizacja obiektu łodzi
            saveHookUpgradeLevel();
            updateMoney();
            hookUpgradeCost *= 2; // Zwiększenie kosztu kolejnego ulepszenia
            saveHookUpgradeCost();
            repaint(); // Odświeżenie widoku sklepu
        } else if (hookUpgradeLevel >= maxHookUpgradeLevel) {
            displayMessage("Haczyk jest już maksymalnie ulepszony!");
        } else {
            displayMessage("Niewystarczająco pieniędzy!");
        }
    }

    /**
     * Obsługuje logikę zakupu ulepszenia łodzi. Działa analogicznie do `upgradeHook`.
     */
    private void upgradeBoat() {
        if (money >= boatUpgradeCost && boatUpgradeLevel < maxBoatUpgradeLevel) {
            money -= boatUpgradeCost;
            boatUpgradeLevel++;
            boatGame.getBoat().setBoatUpgradeLevel(boatUpgradeLevel); // Aktualizacja obiektu łodzi
            saveBoatUpgradeLevel();
            updateMoney();
            boatUpgradeCost *= 2; // Zwiększenie kosztu kolejnego ulepszenia
            saveBoatUpgradeCost();
            repaint(); // Odświeżenie widoku sklepu
        } else if (boatUpgradeLevel >= maxBoatUpgradeLevel) {
            displayMessage("Łódź jest już maksymalnie ulepszona!");
        } else {
            displayMessage("Niewystarczająco pieniędzy!");
        }
    }

    /**
     * Rysuje na panelu graficzną reprezentację poziomu ulepszenia haka
     * oraz przycisk do zakupu kolejnego poziomu.
     * @param g Kontekst graficzny, na którym odbywa się rysowanie.
     */
    private void drawHookUpgrade(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.drawString("Ulepszenie Haczyka:", 10, 20);
        for (int i = 0; i < maxHookUpgradeLevel; i++) {
            g.setColor(i < hookUpgradeLevel ? new Color(0, 200, 0) : new Color(150, 150, 150));
            g.fillRect(10 + i * 30, 30, 20, 20);
            g.setColor(Color.BLACK);
            g.drawRect(10 + i * 30, 30, 20, 20);
        }

        g.setColor(Color.BLACK);
        int plusX = 10 + maxHookUpgradeLevel * 30 + 10;
        int plusY = 30 + 10;
        // Rysowanie "przycisku" w kształcie plusa
        g.fillRect(plusX, plusY - 1, 20, 2);
        g.fillRect(plusX + 9, plusY - 10, 2, 20);
        hookUpgradeButtonRect = new Rectangle(plusX, plusY - 10, 20, 20);

        g.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g.drawString("(" + hookUpgradeCost + " PLN)", plusX - 5, plusY + 25);
    }

    /**
     * Rysuje na panelu graficzną reprezentację poziomu ulepszenia łodzi
     * oraz przycisk do zakupu kolejnego poziomu.
     * @param g Kontekst graficzny, na którym odbywa się rysowanie.
     */
    private void drawBoatUpgrade(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.drawString("Ulepszenie Łodzi:", 10, 80);
        for (int i = 0; i < maxBoatUpgradeLevel; i++) {
            g.setColor(i < boatUpgradeLevel ? new Color(0, 0, 200) : new Color(150, 150, 150));
            g.fillRect(10 + i * 30, 90, 20, 20);
            g.setColor(Color.BLACK);
            g.drawRect(10 + i * 30, 90, 20, 20);
        }

        g.setColor(Color.BLACK);
        int plusX = 10 + maxHookUpgradeLevel * 30 + 10;
        int plusY = 90 + 10;
        g.fillRect(plusX, plusY - 1, 20, 2);
        g.fillRect(plusX + 9, plusY - 10, 2, 20);
        boatUpgradeButtonRect = new Rectangle(plusX, plusY - 10, 20, 20);

        g.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g.drawString("(" + boatUpgradeCost + " PLN)", plusX - 5, plusY + 25);
    }

    /**
     * Resetuje wszystkie zakupione ulepszenia, zwracając graczowi wydane pieniądze.
     * Oblicza sumę wszystkich kosztów, dodaje ją do konta gracza,
     * a następnie zeruje poziomy i koszty ulepszeń.
     */
    private void resetUpgrades() {
        int totalHookCost = 0;
        int currentHookCost = 200;
        for (int i = 0; i < hookUpgradeLevel; i++) {
            totalHookCost += currentHookCost;
            currentHookCost *= 2;
        }

        int totalBoatCost = 0;
        int currentBoatCost = 200;
        for (int i = 0; i < boatUpgradeLevel; i++) {
            totalBoatCost += currentBoatCost;
            currentBoatCost *= 2;
        }

        money += totalHookCost + totalBoatCost;
        hookUpgradeLevel = 0;
        boatUpgradeLevel = 0;
        hookUpgradeCost = 200;
        boatUpgradeCost = 200;

        boatGame.getBoat().setHookUpgradeLevel(hookUpgradeLevel);
        boatGame.getBoat().setBoatUpgradeLevel(boatUpgradeLevel);

        saveHookUpgradeLevel();
        saveBoatUpgradeLevel();
        saveHookUpgradeCost();
        saveBoatUpgradeCost();
        updateMoney();
        repaint();
        displayMessage("Ulepszenia zostały zresetowane!");
    }

    /**
     * Metoda publiczna pozwalająca na aktualizację liczby złowionych ryb w sklepie z zewnątrz.
     * @param fishCount Aktualna liczba ryb posiadanych przez gracza.
     */
    public void updateFishCount(int fishCount) {
        this.fishCaught = fishCount;
        updateLabels();
    }

    /**
     * Obsługuje logikę sprzedaży jednej ryby.
     * Zmniejsza liczbę ryb, zwiększa pieniądze i zapisuje zmiany.
     */
    private void sellFish() {
        if (fishCaught > 0) {
            fishCaught--;
            money += 100;
            updateLabels();
            updateMoney();
            saveFishCaught();
            saveMoney();
            boatGame.updateFishCaught(fishCaught);
        } else {
            displayMessage("Nie masz żadnych ryb do sprzedania!");
        }
    }

    /**
     * Odświeża tekst etykiet z liczbą ryb i pieniędzy.
     */
    private void updateLabels() {
        fishLabel.setText("Złowione ryby: " + fishCaught);
        moneyLabel.setText("Pieniądze: " + money + " PLN");
    }

    /**
     * Aktualizuje stan pieniędzy w głównym obiekcie gry.
     */
    private void updateMoney() {
        boatGame.updateMoneyDisplay(money);
    }

    // --- Poniżej znajdują się metody do wczytywania i zapisywania stanu gry do plików ---

    private void loadFishCaught() {
        try (BufferedReader reader = new BufferedReader(new FileReader(fishFile))) {
            String line = reader.readLine();
            fishCaught = (line != null) ? Integer.parseInt(line.trim()) : 0;
        } catch (IOException | NumberFormatException e) {
            fishCaught = 0;
        }
    }

    private void saveFishCaught() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fishFile))) {
            writer.write(String.valueOf(fishCaught));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMoney() {
        try (BufferedReader reader = new BufferedReader(new FileReader(moneyFile))) {
            String line = reader.readLine();
            money = (line != null) ? Integer.parseInt(line.trim()) : 0;
        } catch (IOException | NumberFormatException e) {
            money = 0;
        }
    }

    private void saveMoney() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(moneyFile))) {
            writer.write(String.valueOf(money));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadHookUpgradeLevel() {
        File file = new File("hook_upgrade.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            hookUpgradeLevel = (line != null) ? Integer.parseInt(line.trim()) : 0;
        } catch (IOException | NumberFormatException e) {
            hookUpgradeLevel = 0;
        }
        boatGame.getBoat().setHookUpgradeLevel(hookUpgradeLevel);
    }

    private void saveHookUpgradeLevel() {
        File file = new File("hook_upgrade.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(String.valueOf(hookUpgradeLevel));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBoatUpgradeLevel() {
        File file = new File("boat_upgrade.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            boatUpgradeLevel = (line != null) ? Integer.parseInt(line.trim()) : 0;
        } catch (IOException | NumberFormatException e) {
            boatUpgradeLevel = 0;
        }
        boatGame.getBoat().setBoatUpgradeLevel(boatUpgradeLevel);
    }

    private void saveBoatUpgradeLevel() {
        File file = new File("boat_upgrade.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(String.valueOf(boatUpgradeLevel));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadHookUpgradeCost() {
        try (BufferedReader reader = new BufferedReader(new FileReader(hookUpgradeCostFile))) {
            String line = reader.readLine();
            hookUpgradeCost = (line != null) ? Integer.parseInt(line.trim()) : 200;
        } catch (IOException | NumberFormatException e) {
            hookUpgradeCost = 200;
        }
    }

    private void saveHookUpgradeCost() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(hookUpgradeCostFile))) {
            writer.write(String.valueOf(hookUpgradeCost));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBoatUpgradeCost() {
        try (BufferedReader reader = new BufferedReader(new FileReader(boatUpgradeCostFile))) {
            String line = reader.readLine();
            boatUpgradeCost = (line != null) ? Integer.parseInt(line.trim()) : 200;
        } catch (IOException | NumberFormatException e) {
            boatUpgradeCost = 200;
        }
    }

    private void saveBoatUpgradeCost() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(boatUpgradeCostFile))) {
            writer.write(String.valueOf(boatUpgradeCost));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Wyświetla komunikat dla gracza w nowym, osobnym oknie,
     * co pozwala uniknąć standardowego wyglądu JOptionPane.
     * @param message Wiadomość do wyświetlenia.
     */
    private void displayMessage(String message) {
        JFrame messageFrame = new JFrame("Komunikat");
        messageFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        messageFrame.setSize(300, 100);
        messageFrame.setLocationRelativeTo(this); // Wyśrodkowanie względem okna sklepu

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        messageFrame.add(messageLabel);

        messageFrame.setVisible(true);
    }
}