package StoreOperations;

import StoreFloor.StorePOS;

/**
 * Simple runtime session holder for the currently active POS (logged-in terminal).
 */
public class Session {
    private static StorePOS currentPOS;

    public static void setCurrentPOS(StorePOS pos){ currentPOS = pos; }
    public static StorePOS getCurrentPOS(){ return currentPOS; }
}
