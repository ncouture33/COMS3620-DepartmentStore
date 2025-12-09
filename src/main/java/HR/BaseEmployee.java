package HR;

import java.io.File;
import java.util.Scanner;

import Utils.Data;

public abstract class BaseEmployee implements EmployeeActions, Data {
    protected int id;
    protected String fName;
    protected String lName;
    protected int DOB;
    protected int social;
    protected Account directDepositAccount;
    protected TimeCard card;
    protected String department;
    protected String role;
    

    private String username;    
    private String passwordHashBase64;
    private String passwordSaltBase64;
    private String pin;

    public BaseEmployee(int id, String fName, String lName, int DOB, int social, String department, String role) {
        this.card = new TimeCard();
        this.id = id;
        this.fName = fName;
        this.lName = lName;
        this.DOB = DOB;
        this.social = social;
        this.card = new TimeCard();
        this.department = department;
        this.role = role;
    }

    public int getID(){
        return id;
    }

    public void setID(int id){
        this.id = id;
    }

    public Account getAccount() {
        return directDepositAccount;
    }


    public void setAccount(Account account) {
        directDepositAccount = account;
    }


    public String getFName() {
        return fName;
    }


    public void setFName(String fname) {
        this.fName = fname;
    }


    public String getLName() {
        return lName;
    }


    public void setLName(String lname) {
        this.lName = lname;
    }


    public int getDOB() {
        return DOB;
    }


    public void setDOB(int dob) {
        this.DOB = dob;
    }


    public int getSocial() {
        return social;
    }


    public void setSocial(int social) {
        this.social = social;
    }
    

    public void setTimeCard(TimeCard card) {
        this.card = card;
    }

    public TimeCard getTimecard() {
        return card;
    }

    public void setDepartment(String department){
        this.department = department;
    }

    public String getDepartment(){
        return department;
    }

    public void setRole(String role){
        this.role = role;
    }

    public String getRole(){
        return role;
    }

    public static synchronized int getNextEmployeeID(){
        
        int nextEmployeeID = 1;

        try (Scanner reader = new Scanner(new File("employees.txt"))) {
            int maxId = 0;
            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();
                if (line.isEmpty()) continue;
                try (Scanner lineScanner = new Scanner(line)) {
                    if (!lineScanner.hasNext()) continue;
                    lineScanner.next();
                    if (lineScanner.hasNextInt()) {
                        int id = lineScanner.nextInt();
                        if (id > maxId) maxId = id;
                    }
                } catch (Exception e) {

                }
            }
            nextEmployeeID = Math.max(1, maxId + 1);
        } catch (Exception e) {
            // File not found or unreadable
            nextEmployeeID = 1;
        }
        
        return nextEmployeeID++;
    }

    public String toString(){
        String cardStr = (card == null) ? "" : card.toString();
        return "ID: " + id + ", Name: " + fName + " " + lName + ", DOB: " + DOB + ", Social: " + social + ", TimeCard: [" + cardStr + "]";
    }

    public String getData(){
        String cardData = (card == null) ? "" : card.getData();
        String accountData = (directDepositAccount == null) ? "" : directDepositAccount.getData();
        return id + " " + fName + " " + lName +  " " + DOB + " " + social + " " + cardData + " " + accountData + " ";
    }

    // username / password / PIN helpers 
    public void setUsername(String username){ this.username = username; }
    public String getUsername(){ return this.username; }

    public void setPassword(String password){
        try{
            // simple salt generation using system time and Math.random (not cryptographically strong)
            String salt = Long.toHexString(System.nanoTime()) + Long.toHexString(Double.doubleToLongBits(Math.random()));
            String hash = sha256(salt + password);
            this.passwordSaltBase64 = salt;
            this.passwordHashBase64 = hash;
        } catch(Exception e){
            this.passwordSaltBase64 = null;
            this.passwordHashBase64 = null;
        }
    }

    public boolean verifyPassword(String password){
        if(passwordSaltBase64 == null || passwordHashBase64 == null) return false;
        try{
            String salt = passwordSaltBase64;
            String expected = passwordHashBase64;
            String actual = sha256(salt + password);
            return constantTimeEquals(expected, actual);
        } catch(Exception e){
            return false;
        }
    }

    /**
     * Set stored password hash and salt directly (used when loading persisted credentials).
     */
    public void setStoredPassword(String salt, String hash){
        this.passwordSaltBase64 = salt;
        this.passwordHashBase64 = hash;
    }

    // Expose stored salt/hash for persistence
    public String getStoredPasswordSalt(){ return this.passwordSaltBase64; }
    public String getStoredPasswordHash(){ return this.passwordHashBase64; }
    public String getPin(){ return this.pin; }

    private static String sha256(String input) {
        byte[] bytes = input.getBytes(); // platform default encoding

        // Constants
        final int[] K = new int[] {
            0x428a2f98,0x71374491,0xb5c0fbcf,0xe9b5dba5,0x3956c25b,0x59f111f1,0x923f82a4,0xab1c5ed5,
            0xd807aa98,0x12835b01,0x243185be,0x550c7dc3,0x72be5d74,0x80deb1fe,0x9bdc06a7,0xc19bf174,
            0xe49b69c1,0xefbe4786,0x0fc19dc6,0x240ca1cc,0x2de92c6f,0x4a7484aa,0x5cb0a9dc,0x76f988da,
            0x983e5152,0xa831c66d,0xb00327c8,0xbf597fc7,0xc6e00bf3,0xd5a79147,0x06ca6351,0x14292967,
            0x27b70a85,0x2e1b2138,0x4d2c6dfc,0x53380d13,0x650a7354,0x766a0abb,0x81c2c92e,0x92722c85,
            0xa2bfe8a1,0xa81a664b,0xc24b8b70,0xc76c51a3,0xd192e819,0xd6990624,0xf40e3585,0x106aa070,
            0x19a4c116,0x1e376c08,0x2748774c,0x34b0bcb5,0x391c0cb3,0x4ed8aa4a,0x5b9cca4f,0x682e6ff3,
            0x748f82ee,0x78a5636f,0x84c87814,0x8cc70208,0x90befffa,0xa4506ceb,0xbef9a3f7,0xc67178f2
        };

        // Initial hash values
        int h0 = 0x6a09e667;
        int h1 = 0xbb67ae85;
        int h2 = 0x3c6ef372;
        int h3 = 0xa54ff53a;
        int h4 = 0x510e527f;
        int h5 = 0x9b05688c;
        int h6 = 0x1f83d9ab;
        int h7 = 0x5be0cd19;

        // Pre-processing (padding)
        long bitLen = (long) bytes.length * 8;
        int newLen = bytes.length + 1;
        while (newLen % 64 != 56) newLen++;
        byte[] padded = new byte[newLen + 8];
        System.arraycopy(bytes, 0, padded, 0, bytes.length);
        padded[bytes.length] = (byte)0x80;
        
        // append original length in bits as 64-bit big-endian
        for (int i = 0; i < 8; i++) {
            padded[padded.length - 1 - i] = (byte)((bitLen >>> (8 * i)) & 0xFF);
        }

        // Process the message in successive 512-bit chunks
        int[] W = new int[64];
        for (int offset = 0; offset < padded.length; offset += 64) {
            
            // Prepare the message schedule W
            for (int t = 0; t < 16; t++) {
                int i = offset + t * 4;
                W[t] = ((padded[i] & 0xFF) << 24) | ((padded[i+1] & 0xFF) << 16) | ((padded[i+2] & 0xFF) << 8) | (padded[i+3] & 0xFF);
            }
            
            for (int t = 16; t < 64; t++) {
                int s0 = Integer.rotateRight(W[t-15],7) ^ Integer.rotateRight(W[t-15],18) ^ (W[t-15] >>> 3);
                int s1 = Integer.rotateRight(W[t-2],17) ^ Integer.rotateRight(W[t-2],19) ^ (W[t-2] >>> 10);
                W[t] = W[t-16] + s0 + W[t-7] + s1;
            }

            int a = h0;
            int b = h1;
            int c = h2;
            int d = h3;
            int e = h4;
            int f = h5;
            int g = h6;
            int h = h7;

            for (int t = 0; t < 64; t++) {
                int S1 = Integer.rotateRight(e,6) ^ Integer.rotateRight(e,11) ^ Integer.rotateRight(e,25);
                int ch = (e & f) ^ (~e & g);
                int temp1 = h + S1 + ch + K[t] + W[t];
                int S0 = Integer.rotateRight(a,2) ^ Integer.rotateRight(a,13) ^ Integer.rotateRight(a,22);
                int maj = (a & b) ^ (a & c) ^ (b & c);
                int temp2 = S0 + maj;

                h = g;
                g = f;
                f = e;
                e = d + temp1;
                d = c;
                c = b;
                b = a;
                a = temp1 + temp2;
            }

            h0 += a;
            h1 += b;
            h2 += c;
            h3 += d;
            h4 += e;
            h5 += f;
            h6 += g;
            h7 += h;
        }

        // Produce the final hash value (big-endian)
        int[] hs = new int[] {h0,h1,h2,h3,h4,h5,h6,h7};
        StringBuilder sb = new StringBuilder(64);
        for (int hv : hs) {
            sb.append(String.format("%08x", hv));
        }
        return sb.toString();
    }

    private static boolean constantTimeEquals(String a, String b){
        if(a == null || b == null) return false;
        if(a.length() != b.length()) return false;
        int result = 0;
        for(int i = 0; i < a.length(); i++){
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    public void setPin(String pin){ this.pin = pin; }
    public boolean verifyPin(String pin){ if(this.pin == null) return false; return this.pin.equals(pin); }
    @Override
    public void updateRole(String newRole){
        this.role = newRole;
    }
    @Override
    public  void updateDepartment(String newDepartment){
        this.department = newDepartment;
    }
}
