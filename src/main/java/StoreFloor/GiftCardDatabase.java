package StoreFloor;

import java.io.*;
import java.util.*;

public class GiftCardDatabase {

    private static final String FILE = "giftcards.txt";

    // Save NEW gift card
    public static void saveGiftCard(GiftCard card) {
        try (FileWriter fw = new FileWriter(FILE, true)) {
            fw.write(card.getData() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load gift card by card number
    public static GiftCard loadGiftCard(String cardNumber) {
        try (Scanner sc = new Scanner(new File(FILE))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(" ");
                if (parts[0].equals(cardNumber)) {
                    GiftCard gc = new GiftCard(parts[0]);
                    gc.loadAmount(Double.parseDouble(parts[1]));
                    return gc;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // not found
    }

    // Update card balance in file
    public static void updateGiftCard(GiftCard updatedCard) {
        File input = new File(FILE);
        File temp = new File("giftcards_temp.txt");

        try (Scanner sc = new Scanner(input);
             PrintWriter pw = new PrintWriter(temp)) {

            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(" ");
                String number = parts[0];

                // Replace old line with updated gift card info
                if (number.equals(updatedCard.getCardNumber())) {
                    pw.println(updatedCard.getData());
                } else {
                    pw.println(line);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        input.delete();
        temp.renameTo(input);
    }

    public static synchronized int getNextGiftCardID(){
        int nextGiftCardID = 1;

        try (Scanner reader = new Scanner(new File(FILE))) {
            int maxId = 0;
            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+");
                if (parts.length == 0) continue;
                try {
                    // first token is the card number (stored as string); try parse as int
                    int id = Integer.parseInt(parts[0]);
                    if (id > maxId) maxId = id;
                } catch (Exception ex) {
                    // ignore malformed or non-integer card numbers
                }
            }
            nextGiftCardID = Math.max(1, maxId + 1);
        } catch (Exception e) {
            // File not found or unreadable
            nextGiftCardID = 1;
        }

        return nextGiftCardID;
    }
}
